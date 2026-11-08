package com.vhvkhangg.personalprivatevault.drive.fixture;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Controlled HTTP server for thumbnail verification and security negatives. */
public class FakeGoogleThumbnailServer implements AutoCloseable {
    public record RequestRecord(String method, String path, String authHeader) {}

    private final HttpServer server;
    private final ExecutorService workers = Executors.newFixedThreadPool(4);
    public final Queue<RequestRecord> requests = new ConcurrentLinkedQueue<>();
    public final AtomicInteger dispatches = new AtomicInteger();

    public volatile int responseStatus = 200;
    public volatile String responseContentType = "image/png";
    public volatile byte[] responseBody = defaultPng();
    public volatile boolean redirect;
    public volatile String redirectLocation;
    public volatile CountDownLatch responseEntered;
    public volatile CountDownLatch responseRelease;
    public volatile int bodyPauseMs;
    public volatile byte[] bodyPrefix;
    public volatile List<byte[]> progressiveChunks;
    public volatile int chunkPauseMs;

    public FakeGoogleThumbnailServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setExecutor(workers);
        server.createContext("/thumbnail/", this::handle);
        server.start();
    }

    public URI origin() {
        return URI.create("http://127.0.0.1:" + server.getAddress().getPort());
    }

    public void reset() {
        requests.clear();
        dispatches.set(0);
        responseStatus = 200;
        responseContentType = "image/png";
        responseBody = defaultPng();
        redirect = false;
        redirectLocation = null;
        responseEntered = null;
        responseRelease = null;
        bodyPauseMs = 0;
        bodyPrefix = null;
        progressiveChunks = null;
        chunkPauseMs = 0;
    }

    private void handle(HttpExchange exchange) throws IOException {
        dispatches.incrementAndGet();
        requests.add(new RequestRecord(
                exchange.getRequestMethod(),
                exchange.getRequestURI().getPath(),
                exchange.getRequestHeaders().getFirst("Authorization")
        ));

        if (responseEntered != null) {
            responseEntered.countDown();
        }
        if (responseRelease != null) {
            try {
                if (!responseRelease.await(10, TimeUnit.SECONDS)) {
                    exchange.close();
                    return;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        try (exchange) {
            if (redirect) {
                exchange.getResponseHeaders().set("Location", redirectLocation != null ? redirectLocation : "/other");
                exchange.sendResponseHeaders(302, -1);
                return;
            }
            exchange.getResponseHeaders().set("Content-Type", responseContentType);
            if (progressiveChunks != null && !progressiveChunks.isEmpty()) {
                long totalLen = progressiveChunks.stream().mapToInt(c -> c.length).sum();
                exchange.sendResponseHeaders(responseStatus, totalLen);
                for (byte[] chunk : progressiveChunks) {
                    exchange.getResponseBody().write(chunk);
                    exchange.getResponseBody().flush();
                    if (chunkPauseMs > 0) {
                        try {
                            Thread.sleep(chunkPauseMs);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
                return;
            }
            byte[] body = responseBody != null ? responseBody : new byte[0];
            if (bodyPauseMs > 0) {
                byte[] prefix = bodyPrefix != null ? bodyPrefix : new byte[0];
                exchange.sendResponseHeaders(responseStatus, prefix.length + body.length);
                if (prefix.length > 0) {
                    exchange.getResponseBody().write(prefix);
                    exchange.getResponseBody().flush();
                }
                try {
                    Thread.sleep(bodyPauseMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                exchange.getResponseBody().write(body);
            } else {
                exchange.sendResponseHeaders(responseStatus, body.length);
                exchange.getResponseBody().write(body);
            }
        }
    }

    public static byte[] defaultPng() {
        try {
            var img = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_RGB);
            var baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(img, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] defaultJpeg() {
        try {
            var img = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_RGB);
            var baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(img, "jpeg", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] defaultWebp() {
        return new byte[]{
                'R', 'I', 'F', 'F',
                12, 0, 0, 0,
                'W', 'E', 'B', 'P',
                'V', 'P', '8', ' ',
                0, 0, 0, 0
        };
    }

    @Override
    public void close() {
        if (responseRelease != null) responseRelease.countDown();
        server.stop(0);
        workers.shutdownNow();
    }
}
