package com.vhvkhangg.personalprivatevault.drive.internal.files.http;

import com.fasterxml.jackson.databind.*;
import com.vhvkhangg.personalprivatevault.drive.internal.files.application.*;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/** Fixed-operation Drive transport. Only controlled test constructors replace its trusted origin. */
public class DriveFileHttpClient implements AutoCloseable {
    private static final ScheduledExecutorService thumbnailWatchdog = Executors.newScheduledThreadPool(1, r -> {
        Thread t = new Thread(r, "ppv-drive-thumbnail-watchdog");
        t.setDaemon(true);
        return t;
    });
    public static final URI GOOGLE = URI.create("https://www.googleapis.com");
    public static final String ROOT_FIELDS = "id,mimeType,parents,trashed,appProperties";
    public static final String FILE_FIELDS = "id,name,mimeType,size,modifiedTime,parents,trashed,appProperties";
    public static final String CHILD_FOLDER_FIELDS = "id,name,mimeType,modifiedTime,parents,trashed,appProperties";

    private final DriveFileAuthority authority;
    private final URI origin;
    private final URI controlledThumbnailOrigin;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2))
            .followRedirects(HttpClient.Redirect.NEVER).version(HttpClient.Version.HTTP_1_1).build();
    private final ObjectMapper mapper = new ObjectMapper();

    public DriveFileHttpClient(DriveFileAuthority authority) {
        this(authority, GOOGLE, null);
    }

    public DriveFileHttpClient(DriveFileAuthority authority, URI controlledOrigin) {
        this(authority, controlledOrigin, null);
    }

    public DriveFileHttpClient(DriveFileAuthority authority, URI controlledOrigin, URI controlledThumbnailOrigin) {
        this.authority = authority;
        this.origin = controlledOrigin;
        this.controlledThumbnailOrigin = controlledThumbnailOrigin;
    }

    public record Media(InputStream input, long declaredLength) {
        @Override public String toString(){return "DriveMedia[REDACTED]";}
    }

    public String accountRoot(DriveFileAuthority.Lease lease, DriveFileResources.Operation op) {
        JsonNode result = json(lease, op, "GET", uri("/drive/v2/about", "fields=rootFolderId"), null, Map.of(), 16 * 1024, false);
        return opaque(result, "rootFolderId", 255);
    }

    public String generateId(DriveFileAuthority.Lease lease, DriveFileResources.Operation op) {
        var result = json(lease, op, "GET", uri("/drive/v3/files/generateIds", "count=1&space=drive&type=files"), null, Map.of(), 16 * 1024, false);
        var ids = result.get("ids");
        if (ids == null || !ids.isArray() || ids.size() != 1 || !ids.get(0).isTextual()) throw failure();
        return DriveFileInputs.generatedId(ids.get(0).textValue());
    }

    public JsonNode root(DriveFileAuthority.Lease lease, DriveFileResources.Operation op, String id) {
        return get(lease, op, id, ROOT_FIELDS);
    }

    public JsonNode childFolder(DriveFileAuthority.Lease lease, DriveFileResources.Operation op, String id) {
        return get(lease, op, id, CHILD_FOLDER_FIELDS);
    }

    public JsonNode file(DriveFileAuthority.Lease lease, DriveFileResources.Operation op, String id, boolean checksum) {
        return get(lease, op, id, FILE_FIELDS + (checksum ? ",md5Checksum" : ""));
    }

    public JsonNode fileWithThumbnail(DriveFileAuthority.Lease lease, DriveFileResources.Operation op, String id) {
        DriveFileInputs.id(id);
        return json(lease, op, "GET", uri("/drive/v3/files/" + id, "fields=" + encode(FILE_FIELDS + ",thumbnailLink")), null, Map.of(), 64 * 1024, false);
    }

    private JsonNode get(DriveFileAuthority.Lease lease, DriveFileResources.Operation op, String id, String fields) {
        DriveFileInputs.id(id);
        return json(lease, op, "GET", uri("/drive/v3/files/" + id, "fields=" + encode(fields)), null, Map.of(), 64 * 1024, false);
    }

    public JsonNode createRoot(DriveFileAuthority.Lease lease, DriveFileResources.Operation op, String id, UUID binding) {
        var metadata = Map.of("id", DriveFileInputs.generatedId(id), "name", "PPV", "mimeType", "application/vnd.google-apps.folder",
                "parents", List.of("root"), "appProperties", Map.of("ppvKind", "root", "ppvRootBinding", binding.toString(), "ppvCreatedId", id));
        return json(lease, op, "POST", uri("/drive/v3/files", "fields=" + encode(ROOT_FIELDS)), bytes(metadata), Map.of(), 64 * 1024, false);
    }

    public JsonNode createChildFolder(DriveFileAuthority.Lease lease, DriveFileResources.Operation op,
                                      String id, String safeName, UUID binding, String action) {
        Map<String, String> appProps = new HashMap<>();
        appProps.put("ppvKind", "child");
        appProps.put("ppvRootBinding", binding.toString());
        appProps.put("ppvCreatedId", id);
        appProps.put("ppvFolderAction", action);
        appProps.put("ppvParentId", lease.root().getRootFileId());
        for (var entry : appProps.entrySet()) {
            if ((entry.getKey() + entry.getValue()).getBytes(StandardCharsets.UTF_8).length > 124) throw failure();
        }
        var metadata = Map.of(
                "id", DriveFileInputs.generatedId(id),
                "name", safeName,
                "mimeType", "application/vnd.google-apps.folder",
                "parents", List.of(lease.root().getRootFileId()),
                "appProperties", appProps
        );
        return json(lease, op, "POST", uri("/drive/v3/files", "fields=" + encode(CHILD_FOLDER_FIELDS)), bytes(metadata), Map.of(), 64 * 1024, false);
    }

    public JsonNode list(DriveFileAuthority.Lease lease, DriveFileResources.Operation op, String root, int limit, String token) {
        DriveFileInputs.id(root);
        String query = "q=" + encode("'" + root + "' in parents and trashed = false") + "&pageSize=" + limit
                + "&fields=" + encode("nextPageToken,files(" + FILE_FIELDS + ")") + (token == null ? "" : "&pageToken=" + encode(token));
        return json(lease, op, "GET", uri("/drive/v3/files", query), null, Map.of(), 1024 * 1024, false);
    }

    public JsonNode listItems(DriveFileAuthority.Lease lease, DriveFileResources.Operation op, String query, int limit, String token) {
        String params = "corpora=user&spaces=drive&q=" + encode(query) + "&pageSize=" + limit + "&orderBy=" + encode("modifiedTime desc,name")
                + "&fields=" + encode("incompleteSearch,nextPageToken,files(" + FILE_FIELDS + ")")
                + (token == null ? "" : "&pageToken=" + encode(token));
        return json(lease, op, "GET", uri("/drive/v3/files", params), null, Map.of(), 1024 * 1024, false);
    }

    public JsonNode storageQuota(DriveFileAuthority.Lease lease, DriveFileResources.Operation op) {
        return json(lease, op, "GET", uri("/drive/v3/about", "fields=" + encode("storageQuota")), null, Map.of(), 16 * 1024, false);
    }

    public JsonNode upload(DriveFileAuthority.Lease lease, DriveFileResources.Operation op,
                           DriveMultipartUpload.Spool spool, String id, UUID binding, String action) {
        return upload(lease, op, spool, id, binding, action, null);
    }

    public JsonNode upload(DriveFileAuthority.Lease lease, DriveFileResources.Operation op,
                           DriveMultipartUpload.Spool spool, String id, UUID binding, String action, String targetParentId) {
        String parent = targetParentId != null ? targetParentId : lease.root().getRootFileId();
        Map<String, String> appProps = new HashMap<>();
        appProps.put("ppvKind", "file");
        appProps.put("ppvRootBinding", binding.toString());
        appProps.put("ppvCreatedId", id);
        appProps.put("ppvUploadAction", action);
        if (targetParentId != null) {
            appProps.put("ppvParentId", targetParentId);
        }
        for (var entry : appProps.entrySet()) {
            if ((entry.getKey() + entry.getValue()).getBytes(StandardCharsets.UTF_8).length > 124) throw failure();
        }
        var metadata = Map.of("id", DriveFileInputs.generatedId(id), "name", spool.name(), "mimeType", spool.mime(),
                "parents", List.of(parent), "appProperties", appProps);
        String fields = encode(FILE_FIELDS + ",md5Checksum");
        if (spool.size() <= 5_000_000) {
            String boundary = "ppv_" + UUID.randomUUID();
            byte[] head = ("--" + boundary + "\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n" + new String(bytes(metadata), StandardCharsets.UTF_8)
                    + "\r\n--" + boundary + "\r\nContent-Type: " + spool.mime() + "\r\n\r\n").getBytes(StandardCharsets.UTF_8);
            byte[] tail = ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.US_ASCII);
            var publisher = HttpRequest.BodyPublishers.concat(HttpRequest.BodyPublishers.ofByteArray(head), filePublisher(spool, op), HttpRequest.BodyPublishers.ofByteArray(tail));
            return jsonPublisher(lease, op, "POST", uri("/upload/drive/v3/files", "uploadType=multipart&fields=" + fields), publisher,
                    Map.of("Content-Type", "multipart/related; boundary=" + boundary), 64 * 1024, true);
        }
        var session = send(lease, op, "POST", uri("/upload/drive/v3/files", "uploadType=resumable&fields=" + fields),
                HttpRequest.BodyPublishers.ofByteArray(bytes(metadata)), Map.of("Content-Type", "application/json; charset=UTF-8",
                        "X-Upload-Content-Length", Long.toString(spool.size()), "X-Upload-Content-Type", spool.mime()), 64 * 1024, false);
        success(session.statusCode(), session.body(), lease);
        URI location = validateSession(session.headers().firstValue("Location").orElse(null));
        return jsonPublisher(lease, op, "PUT", location, filePublisher(spool, op), Map.of("Content-Type", spool.mime()), 64 * 1024, true);
    }

    private HttpRequest.BodyPublisher filePublisher(DriveMultipartUpload.Spool spool, DriveFileResources.Operation op) {
        if (spool.size() == 0) return HttpRequest.BodyPublishers.noBody();
        var streaming = HttpRequest.BodyPublishers.ofInputStream(() -> {
            try { return op.track(Files.newInputStream(spool.path())); }
            catch(IOException e) { throw DriveFileException.input(); }
        });
        return HttpRequest.BodyPublishers.fromPublisher(streaming, spool.size());
    }

    public URI validateSession(String value) {
        try {
            if (value == null || value.length() > 4096) throw failure();
            URI uri = URI.create(value);
            int expected = origin.getPort() < 0 ? (origin.getScheme().equals("https") ? 443 : 80) : origin.getPort();
            int port = uri.getPort() < 0 ? ("https".equals(uri.getScheme()) ? 443 : 80) : uri.getPort();
            if (!Objects.equals(uri.getScheme(), origin.getScheme()) || !Objects.equals(uri.getHost(), origin.getHost())
                    || port != expected || uri.getRawUserInfo() != null || uri.getRawFragment() != null
                    || !"/upload/drive/v3/files".equals(uri.getRawPath()) || uri.getRawQuery() == null) throw failure();
            var query = new HashMap<String, String>();
            for (String pair : uri.getRawQuery().split("&", -1)) {
                String[] parts = pair.split("=", 2);
                if (parts.length != 2 || query.putIfAbsent(parts[0], parts[1]) != null) throw failure();
            }
            if (query.size() != 2 || !"resumable".equals(query.get("uploadType")) || query.get("upload_id") == null
                    || !query.get("upload_id").matches("[A-Za-z0-9._~-]{1,3900}")) throw failure();
            return uri;
        } catch(IllegalArgumentException e) { throw failure(); }
    }

    public Media media(DriveFileAuthority.Lease lease, DriveFileResources.Operation op, String id) {
        DriveFileInputs.id(id); op.metadataPhase();
        CompletableFuture<HttpResponse<InputStream>> future = null;
        try {
            var request = request(lease, op, "GET", uri("/drive/v3/files/" + id, "alt=media"), HttpRequest.BodyPublishers.noBody(), Map.of());
            future = authority.dispatch(lease, op, () -> client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream()));
            var current = future; op.track(() -> current.cancel(true));
            var response = future.get(op.remaining().toNanos(), TimeUnit.NANOSECONDS);
            headerLimit(response.headers()); var input = op.track(response.body());
            if (response.statusCode() != 200) { byte[] error = input.readNBytes(64 * 1024 + 1); input.close(); success(response.statusCode(), error, lease); throw failure(); }
            String encoding = response.headers().firstValue("Content-Encoding").orElse("identity");
            if (!encoding.equalsIgnoreCase("identity")) throw failure();
            long length = response.headers().firstValueAsLong("Content-Length").orElse(-1);
            if (length > DriveFileResources.MAX_FILE || length < -1) throw failure();
            op.beginBody(); return new Media(input, length);
        } catch(InterruptedException e) { Thread.currentThread().interrupt(); throw DriveFileException.timeout(); }
        catch(TimeoutException | CancellationException e) { throw DriveFileException.timeout(); }
        catch(ExecutionException | IOException | IllegalArgumentException e) { op.check(); throw failure(); }
        finally { if (future != null && !future.isDone()) future.cancel(true); }
    }

    public boolean isTrustedThumbnailUri(URI uri) {
        if (uri == null) return false;
        if (controlledThumbnailOrigin != null) {
            int expectedPort = controlledThumbnailOrigin.getPort() < 0 ? (controlledThumbnailOrigin.getScheme().equals("https") ? 443 : 80) : controlledThumbnailOrigin.getPort();
            int port = uri.getPort() < 0 ? ("https".equals(uri.getScheme()) ? 443 : 80) : uri.getPort();
            return Objects.equals(uri.getScheme(), controlledThumbnailOrigin.getScheme())
                    && Objects.equals(uri.getHost(), controlledThumbnailOrigin.getHost())
                    && port == expectedPort
                    && uri.getRawUserInfo() == null
                    && uri.getRawFragment() == null;
        }
        return isProductionThumbnailUri(uri);
    }

    public static boolean isProductionThumbnailUri(URI uri) {
        if (uri == null) return false;
        if (!"https".equalsIgnoreCase(uri.getScheme())) return false;
        if (uri.getHost() == null || !"lh3.googleusercontent.com".equalsIgnoreCase(uri.getHost())) return false;
        int port = uri.getPort();
        if (port != -1 && port != 443) return false;
        if (uri.getRawUserInfo() != null || uri.getRawFragment() != null) return false;
        return true;
    }

    public static boolean isValidProductionThumbnailUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank() || rawUrl.length() > 4096) return false;
        try {
            URI uri = URI.create(rawUrl);
            return isProductionThumbnailUri(uri);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public DriveThumbnailService.ThumbnailImage fetchThumbnail(
            DriveFileAuthority.Lease lease, DriveFileResources.Operation op, String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank() || rawUrl.length() > 4096) {
            throw new DriveFileException("THUMBNAIL_UNAVAILABLE", 404);
        }
        URI uri;
        try {
            uri = URI.create(rawUrl);
        } catch (IllegalArgumentException e) {
            throw new DriveFileException("THUMBNAIL_UNAVAILABLE", 404);
        }
        if (!isTrustedThumbnailUri(uri)) {
            throw new DriveFileException("THUMBNAIL_UNAVAILABLE", 404);
        }
        op.metadataPhase();
        CompletableFuture<HttpResponse<byte[]>> future = null;
        var lastProgress = new AtomicLong(System.nanoTime());
        var idleExpired = new AtomicBoolean(false);
        ScheduledFuture<?> idleTask = null;
        try {
            var request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(8))
                    .header("Authorization", "Bearer " + lease.token())
                    .header("Accept-Encoding", "identity")
                    .GET()
                    .build();
            future = authority.dispatch(lease, op, () -> client.sendAsync(request, info -> {
                headerLimit(info.headers());
                var delegate = HttpResponse.BodySubscribers.limiting(HttpResponse.BodySubscribers.ofByteArray(), 1_048_576 + 1);
                return new HttpResponse.BodySubscriber<byte[]>() {
                    public CompletionStage<byte[]> getBody(){return delegate.getBody();}
                    public void onSubscribe(Flow.Subscription sub){op.track(() -> sub.cancel());delegate.onSubscribe(sub);}
                    public void onNext(List<ByteBuffer> items){
                        op.check();
                        lastProgress.set(System.nanoTime());
                        op.progress();
                        delegate.onNext(items);
                    }
                    public void onError(Throwable error){delegate.onError(error);}
                    public void onComplete(){delegate.onComplete();}
                };
            }));
            var current = future;
            op.track(() -> current.cancel(true));
            idleTask = thumbnailWatchdog.scheduleWithFixedDelay(() -> {
                if (System.nanoTime() - lastProgress.get() > TimeUnit.SECONDS.toNanos(5)) {
                    idleExpired.set(true);
                    current.cancel(true);
                }
            }, 500, 200, TimeUnit.MILLISECONDS);
            var response = future.get(Math.min(op.remaining().toNanos(), TimeUnit.SECONDS.toNanos(8)), TimeUnit.NANOSECONDS);
            op.check();
            if (response.statusCode() != 200) {
                throw new DriveFileException("THUMBNAIL_UNAVAILABLE", 404);
            }
            if (response.headers().firstValue("Content-Encoding").isPresent()) {
                throw new DriveFileException("THUMBNAIL_UNAVAILABLE", 404);
            }
            byte[] body = response.body();
            if (body == null || body.length == 0 || body.length > 1_048_576) {
                throw new DriveFileException("THUMBNAIL_UNAVAILABLE", 404);
            }
            String rawContentType = response.headers().firstValue("Content-Type").orElse("");
            int semi = rawContentType.indexOf(';');
            String mime = (semi > 0 ? rawContentType.substring(0, semi) : rawContentType).strip().toLowerCase(Locale.ROOT);
            if (!Set.of("image/png", "image/jpeg", "image/webp").contains(mime)) {
                throw new DriveFileException("THUMBNAIL_UNAVAILABLE", 404);
            }
            if (!verifyImageSignature(body, mime)) {
                throw new DriveFileException("THUMBNAIL_UNAVAILABLE", 404);
            }
            return new DriveThumbnailService.ThumbnailImage(body, mime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DriveFileException("THUMBNAIL_UNAVAILABLE", 404);
        } catch (TimeoutException | CancellationException e) {
            throw new DriveFileException("THUMBNAIL_UNAVAILABLE", 404);
        } catch (ExecutionException | IllegalArgumentException e) {
            op.check();
            throw new DriveFileException("THUMBNAIL_UNAVAILABLE", 404);
        } finally {
            if (idleTask != null) idleTask.cancel(true);
            if (future != null && !future.isDone()) future.cancel(true);
            op.endPhase();
        }
    }

    public static boolean verifyImageSignature(byte[] body, String mime) {
        if (body == null || body.length == 0 || body.length > 1_048_576) return false;
        try {
            if ("image/png".equals(mime)) {
                if (body.length < 24) return false;
                if (body[0] != (byte) 0x89 || body[1] != (byte) 'P' || body[2] != (byte) 'N'
                        || body[3] != (byte) 'G' || body[4] != (byte) 0x0D || body[5] != (byte) 0x0A
                        || body[6] != (byte) 0x1A || body[7] != (byte) 0x0A) {
                    return false;
                }
                // Pre-decode dimensions check from IHDR chunk to prevent decoder allocation
                if (body[12] != 'I' || body[13] != 'H' || body[14] != 'D' || body[15] != 'R') {
                    return false;
                }
                int width = ((body[16] & 0xFF) << 24) | ((body[17] & 0xFF) << 16)
                        | ((body[18] & 0xFF) << 8) | (body[19] & 0xFF);
                int height = ((body[20] & 0xFF) << 24) | ((body[21] & 0xFF) << 16)
                        | ((body[22] & 0xFF) << 8) | (body[23] & 0xFF);
                if (width <= 0 || height <= 0 || width > 2048 || height > 2048) {
                    return false;
                }
                // IEND chunk check
                if (body.length < 12 || body[body.length - 8] != 'I'
                        || body[body.length - 7] != 'E' || body[body.length - 6] != 'N' || body[body.length - 5] != 'D') {
                    return false;
                }
                var img = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(body));
                return img != null && img.getWidth() > 0 && img.getHeight() > 0
                        && img.getWidth() <= 2048 && img.getHeight() <= 2048;
            } else if ("image/jpeg".equals(mime)) {
                if (body.length < 4) return false;
                if (body[0] != (byte) 0xFF || body[1] != (byte) 0xD8
                        || body[body.length - 2] != (byte) 0xFF || body[body.length - 1] != (byte) 0xD9) {
                    return false;
                }
                // Pre-decode dimensions check from SOF marker
                int offset = 2;
                boolean foundSof = false;
                int width = -1;
                int height = -1;
                while (offset < body.length - 1) {
                    if ((body[offset] & 0xFF) != 0xFF) {
                        offset++;
                        continue;
                    }
                    int marker = body[offset + 1] & 0xFF;
                    offset += 2;
                    if (marker == 0xD8 || marker == 0x01 || (marker >= 0xD0 && marker <= 0xD7)) {
                        continue;
                    }
                    if (marker == 0xD9 || marker == 0xDA) {
                        break;
                    }
                    if (offset + 2 > body.length) break;
                    int length = ((body[offset] & 0xFF) << 8) | (body[offset + 1] & 0xFF);
                    if (length < 2 || offset + length > body.length) break;
                    if ((marker >= 0xC0 && marker <= 0xC3) || (marker >= 0xC5 && marker <= 0xC7)
                            || (marker >= 0xC9 && marker <= 0xCB) || (marker >= 0xCD && marker <= 0xCF)) {
                        if (length >= 7) {
                            height = ((body[offset + 3] & 0xFF) << 8) | (body[offset + 4] & 0xFF);
                            width = ((body[offset + 5] & 0xFF) << 8) | (body[offset + 6] & 0xFF);
                            foundSof = true;
                        }
                        break;
                    }
                    offset += length;
                }
                if (!foundSof || width <= 0 || height <= 0 || width > 2048 || height > 2048) {
                    return false;
                }
                var readers = javax.imageio.ImageIO.getImageReadersByMIMEType("image/jpeg");
                if (!readers.hasNext()) {
                    return false;
                }
                var reader = readers.next();
                try {
                    try (var iis = javax.imageio.ImageIO.createImageInputStream(new java.io.ByteArrayInputStream(body))) {
                        if (iis == null) {
                            return false;
                        }
                        var warnings = new java.util.concurrent.atomic.AtomicBoolean(false);
                        reader.addIIOReadWarningListener((source, warning) -> warnings.set(true));
                        reader.setInput(iis);
                        var img = reader.read(0);
                        if (warnings.get()) {
                            return false;
                        }
                        return img != null && img.getWidth() > 0 && img.getHeight() > 0
                                && img.getWidth() <= 2048 && img.getHeight() <= 2048;
                    }
                } finally {
                    reader.dispose();
                }
            } else if ("image/webp".equals(mime)) {
                // Runtime check: standard JDK ImageIO does not bundle WebP decoder.
                // Unless an ImageReader is registered in the runtime to verify complete decodability,
                // unsupported WEBP falls back.
                var readers = javax.imageio.ImageIO.getImageReadersByMIMEType("image/webp");
                if (!readers.hasNext()) {
                    return false;
                }
                var reader = readers.next();
                try (var iis = javax.imageio.ImageIO.createImageInputStream(new java.io.ByteArrayInputStream(body))) {
                    reader.setInput(iis);
                    int width = reader.getWidth(0);
                    int height = reader.getHeight(0);
                    if (width <= 0 || height <= 0 || width > 2048 || height > 2048) {
                        return false;
                    }
                    var img = reader.read(0);
                    return img != null && img.getWidth() > 0 && img.getHeight() > 0
                            && img.getWidth() <= 2048 && img.getHeight() <= 2048;
                } finally {
                    reader.dispose();
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private JsonNode json(DriveFileAuthority.Lease lease, DriveFileResources.Operation op, String method, URI uri,
                          byte[] body, Map<String, String> headers, int limit, boolean transfer) {
        var publisher = body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofByteArray(body);
        var selected = body != null && headers.isEmpty() ? Map.of("Content-Type", "application/json; charset=UTF-8") : headers;
        return jsonPublisher(lease, op, method, uri, publisher, selected, limit, transfer);
    }

    private JsonNode jsonPublisher(DriveFileAuthority.Lease lease, DriveFileResources.Operation op, String method, URI uri,
                                   HttpRequest.BodyPublisher body, Map<String, String> headers, int limit, boolean transfer) {
        var response = send(lease, op, method, uri, body, headers, limit, transfer);
        success(response.statusCode(), response.body(), lease);
        try { JsonNode result = mapper.readTree(response.body()); if (result == null || !result.isObject()) throw failure(); return result; }
        catch(IOException e) { throw failure(); }
    }

    private HttpResponse<byte[]> send(DriveFileAuthority.Lease lease, DriveFileResources.Operation op, String method, URI uri,
                                      HttpRequest.BodyPublisher body, Map<String, String> headers, int limit, boolean transfer) {
        if (transfer) op.beginBody(); else op.metadataPhase();
        CompletableFuture<HttpResponse<byte[]>> future = null;
        try {
            var publisher = transfer ? progressPublisher(body, op) : body;
            var request = request(lease, op, method, uri, publisher, headers);
            future = authority.dispatch(lease, op, () -> client.sendAsync(request, info -> {
                headerLimit(info.headers());
                var delegate = HttpResponse.BodySubscribers.limiting(HttpResponse.BodySubscribers.ofByteArray(), limit);
                return new HttpResponse.BodySubscriber<byte[]>() {
                    public CompletionStage<byte[]> getBody(){return delegate.getBody();}
                    public void onSubscribe(Flow.Subscription sub){op.track(() -> sub.cancel());delegate.onSubscribe(sub);}
                    public void onNext(List<ByteBuffer> items){op.check();op.progress();delegate.onNext(items);}
                    public void onError(Throwable error){delegate.onError(error);}
                    public void onComplete(){delegate.onComplete();}
                };
            }));
            var current = future; op.track(() -> current.cancel(true));
            var result = future.get(op.remaining().toNanos(), TimeUnit.NANOSECONDS); op.check(); return result;
        } catch(InterruptedException e) { Thread.currentThread().interrupt(); throw DriveFileException.timeout(); }
        catch(TimeoutException | CancellationException e) { throw DriveFileException.timeout(); }
        catch(ExecutionException | IllegalArgumentException e) { op.check(); throw failure(); }
        finally { if (future != null && !future.isDone()) future.cancel(true); op.endPhase(); }
    }

    private HttpRequest request(DriveFileAuthority.Lease lease, DriveFileResources.Operation op, String method, URI uri, HttpRequest.BodyPublisher body, Map<String, String> headers) {
        var builder = HttpRequest.newBuilder(uri).timeout(op.remaining()).header("Authorization", "Bearer " + lease.token()).header("Accept-Encoding", "identity");
        headers.forEach(builder::header); return builder.method(method, body).build();
    }

    private HttpRequest.BodyPublisher progressPublisher(HttpRequest.BodyPublisher body, DriveFileResources.Operation op) {
        return new HttpRequest.BodyPublisher() {
            public long contentLength(){return body.contentLength();}
            public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) {
                body.subscribe(new Flow.Subscriber<>() {
                    public void onSubscribe(Flow.Subscription sub){op.track(() -> sub.cancel());subscriber.onSubscribe(sub);}
                    public void onNext(ByteBuffer value){op.check();op.progress();subscriber.onNext(value);}
                    public void onError(Throwable error){subscriber.onError(error);}
                    public void onComplete(){op.metadataPhase();subscriber.onComplete();}
                });
            }
        };
    }

    private void success(int status, byte[] body, DriveFileAuthority.Lease lease) {
        if (status >= 200 && status < 300) return;
        if (status == 401) { authority.invalidate(lease); throw new DriveFileException("DRIVE_RECONNECT_REQUIRED", 409); }
        if (status == 429) throw new DriveFileException("DRIVE_PROVIDER_RATE_LIMITED", 503);
        if (status == 403) {
            try {
                var reasons = mapper.readTree(body).path("error").path("errors");
                if (reasons.isArray()) for(var value : reasons) if (Set.of("storageQuotaExceeded", "userRateLimitExceeded", "rateLimitExceeded", "dailyLimitExceeded", "downloadQuotaExceeded", "quotaExceeded").contains(value.path("reason").asText())) {
                    throw new DriveFileException("DRIVE_PROVIDER_QUOTA", 503);
                }
            } catch(IOException ignored) { /* no provider diagnostics retained */ }
        }
        if (status == 403 || status == 404) throw new DriveFileException("DRIVE_FILE_UNAVAILABLE", 404);
        throw failure();
    }

    private void headerLimit(HttpHeaders headers) {
        long bytes = 0;
        for (var header : headers.map().entrySet()) for (String value : header.getValue()) bytes += header.getKey().getBytes(StandardCharsets.UTF_8).length + value.getBytes(StandardCharsets.UTF_8).length + 4;
        if (bytes > 16 * 1024) throw failure();
    }

    private String opaque(JsonNode result, String field, int max) {
        var value = result.get(field); if (value == null || !value.isTextual() || value.textValue().length() > max) throw failure();
        try { return DriveFileInputs.id(value.textValue()); } catch(DriveFileException e) { throw failure(); }
    }

    private URI uri(String path, String query) { return origin.resolve(path + "?" + query); }
    private String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }
    private byte[] bytes(Object value) { try { return mapper.writeValueAsBytes(value); } catch(IOException e) { throw failure(); } }
    private DriveFileException failure() { return new DriveFileException("DRIVE_PROVIDER_FAILURE", 502); }

    @Override public void close() { client.shutdownNow(); }
}
