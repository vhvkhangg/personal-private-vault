package com.vhvkhangg.personalprivatevault.drive;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vhvkhangg.personalprivatevault.drive.fixture.DriveS2HttpTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class DriveS2WebIntegrationTest extends DriveS2HttpTestSupport {
    @Test void realHttpRootUploadListAndAttachmentPreserveOriginalBytesAndSafeProjection() throws Exception {
        byte[] bytes={0,1,2,3,(byte)255,13,10};String name="résumé 旅行.zip";
        String id=uploaded(bytes,name);
        var list=send(request(path(1)+"/files?limit=25").GET());assertThat(list.statusCode()).isEqualTo(200);
        var file=data(list).path("files").get(0);assertThat(file.fieldNames()).toIterable().containsExactlyInAnyOrder("id","name","mimeType","sizeBytes","modifiedAt");
        assertThat(file.path("modifiedAt").isTextual()).isTrue();assertThat(file.path("name").asText()).isEqualTo(name);
        String json=new String(list.body(),StandardCharsets.UTF_8);assertThat(json).doesNotContain("googleSub","ppvRootBinding","credentialEpoch","access-","refresh-","appProperties","md5Checksum");
        var download=send(request(path(1)+"/files/"+id+"/download").GET());
        assertThat(download.statusCode()).isEqualTo(200);assertThat(download.body()).isEqualTo(bytes);
        assertThat(download.headers().firstValue("Content-Type")).hasValue("application/octet-stream");
        assertThat(download.headers().firstValue("Cache-Control")).hasValue("no-store");
        assertThat(download.headers().firstValue("X-Content-Type-Options")).hasValue("nosniff");
        assertThat(download.headers().firstValue("Content-Disposition").orElseThrow()).startsWith("attachment;").contains("filename*=UTF-8''r%C3%A9sum%C3%A9%20");
        var range=send(request(path(1)+"/files/"+id+"/download").header("Range","bytes=0-1").GET());assertThat(range.statusCode()).isEqualTo(400);
    }
    @ParameterizedTest @ValueSource(strings={"pdf","zip","png","mp4","md","docx","xlsx"})
    void supportedOriginalTypesAreUploadedWithoutConversion(String extension) throws Exception {
        ensure(1);String mime=switch(extension){case "pdf"->"application/pdf";case "png"->"image/png";case "mp4"->"video/mp4";case "md"->"text/markdown";case "docx"->"application/vnd.openxmlformats-officedocument.wordprocessingml.document";case "xlsx"->"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";default->"application/zip";};
        byte[] content=("original-"+extension).getBytes(StandardCharsets.UTF_8);String key=key();
        var result=upload(1,key,content," "+extension+"."+extension+" ",mime);assertThat(result.statusCode()).isEqualTo(201);
        assertThat(data(result).path("name").asText()).isEqualTo(extension+"."+extension);
        var repeat=upload(1,key,content,extension+"."+extension,mime);assertThat(repeat.statusCode()).isEqualTo(200);
        assertThat(data(repeat).path("id").asText()).isEqualTo(data(result).path("id").asText());
        assertThat(GOOGLE.fileCreates).hasValue(1);
    }
    @ParameterizedTest @ValueSource(strings={"absent","malformed","google","expired"})
    void allFourEndpointsRequireValidPpvJwtBeforeProviderOrSpoolActivity(String token) throws Exception {
        String bearer=switch(token){
            case "absent"->null;case "malformed"->"broken";case "google"->OAUTH.createSignedIdToken("U","u@example.com","s2-http-client","nonce",Instant.now().plusSeconds(300));
            default->encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),JwtClaimsSet.builder().issuer("personal-private-vault").subject("1").issuedAt(Instant.now().minusSeconds(400)).expiresAt(Instant.now().minusSeconds(300)).build())).getTokenValue();
        };
        for(String suffix:List.of("/root-folder","/files","/files/file-1/download")){
            var builder=HttpRequest.newBuilder(uri(path(1)+suffix)).timeout(Duration.ofSeconds(3));
            if(bearer!=null)builder.header("Authorization","Bearer "+bearer);
            if(suffix.equals("/root-folder"))builder.POST(HttpRequest.BodyPublishers.noBody());else builder.GET();
            assertThat(send(builder).statusCode()).isEqualTo(401);
        }
        var upload=HttpRequest.newBuilder(uri(path(1)+"/files")).POST(HttpRequest.BodyPublishers.noBody());
        if(bearer!=null)upload.header("Authorization","Bearer "+bearer);
        assertThat(send(upload).statusCode()).isEqualTo(401);
        assertThat(GOOGLE.calls).isEmpty();assertThat(resources.activeSpools()).isZero();
    }
    @ParameterizedTest @ValueSource(strings={"0","3","x"})
    void invalidSlotsRejectBeforeRefresh(String slot) throws Exception {
        int refresh=OAUTH.refreshTokenCount.get();
        assertThat(send(request("/api/v1/drive/connections/"+slot+"/root-folder").POST(HttpRequest.BodyPublishers.noBody())).statusCode()).isEqualTo(400);
        assertThat(GOOGLE.calls).isEmpty();assertThat(OAUTH.refreshTokenCount).hasValue(refresh);
    }
    @ParameterizedTest @ValueSource(strings={"limit=0","limit=51","limit=x","limit=1&limit=2","cursor=provider-secret","q=whole-drive"})
    void malformedPagingHasNoProviderActivity(String query) throws Exception {
        assertThat(send(request(path(1)+"/files?"+query).GET()).statusCode()).isEqualTo(400);assertThat(GOOGLE.calls).isEmpty();
    }
    @Test void crossAccountFileIdDoesNotReadMediaEvenWhenProviderWouldAllowIt() throws Exception {
        String id=uploaded(new byte[]{9},"private.bin");ensure(2);GOOGLE.allowForeignMetadata=true;GOOGLE.allowForeignMedia=true;
        assertThat(send(request(path(2)+"/files/"+id+"/download").GET()).statusCode()).isEqualTo(404);assertThat(GOOGLE.mediaCalls).hasValue(0);
    }
    @ParameterizedTest @ValueSource(strings={"../escape","a\\b","..","   ","control\u0001.bin"})
    void unsafeUploadNamesRejectBeforeProviderOrIntentCreation(String name) throws Exception {
        var response=upload(1,key(),new byte[]{1},name,"application/octet-stream");
        assertThat(response.statusCode()).isEqualTo(400);assertThat(GOOGLE.calls).isEmpty();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM drive_upload_intents",Integer.class)).isZero();
    }
    @Test void nativeMimeMissingKeyAndMultiplePartsNeverDispatchProviderRequests() throws Exception {
        assertThat(upload(1,key(),new byte[0],"native","application/vnd.google-apps.document").statusCode()).isEqualTo(415);
        assertThat(send(request(path(1)+"/files").header("Content-Type","multipart/form-data; boundary=x").POST(HttpRequest.BodyPublishers.noBody())).statusCode()).isEqualTo(400);
        String body="--x\r\nContent-Disposition: form-data; name=\"file\"; filename=\"one\"\r\n\r\na\r\n--x\r\nContent-Disposition: form-data; name=\"file\"; filename=\"two\"\r\n\r\nb\r\n--x--\r\n";
        assertThat(send(request(path(1)+"/files").header("Content-Type","multipart/form-data; boundary=x").header("Idempotency-Key",key())
                .POST(HttpRequest.BodyPublishers.ofString(body))).statusCode()).isEqualTo(400);
        assertThat(GOOGLE.calls).isEmpty();
    }
    @Test void correctlySignedPpvTokenForAnotherSubjectCannotUseDriveAuthority() throws Exception {
        String token=encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),
                JwtClaimsSet.builder().issuer("personal-private-vault").subject("2").issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(300)).build())).getTokenValue();
        var response=send(HttpRequest.newBuilder(uri(path(1)+"/root-folder")).header("Authorization","Bearer "+token).POST(HttpRequest.BodyPublishers.noBody()));
        assertThat(response.statusCode()).isEqualTo(401);assertThat(GOOGLE.calls).isEmpty();
    }
    @Test void actualOpenApiHas243OperationsAndBinarySchemasWhileCallbackRemainsPublicGetOnly() throws Exception {
        var response=send(HttpRequest.newBuilder(uri("/v3/api-docs")).GET());assertThat(response.statusCode()).isEqualTo(200);
        var document=mapper.readTree(response.body());var paths=document.path("paths");int count=0;
        for(var item:paths){for(var names=item.fieldNames();names.hasNext();)if(Set.of("get","post","put","patch","delete").contains(names.next()))count++;}
        assertThat(count).isEqualTo(252);
        var upload=paths.path("/api/v1/drive/connections/{slot}/files").path("post");
        assertThat(upload.path("security").toString()).contains("bearerAuth");
        String ref=upload.path("requestBody").path("content").path("multipart/form-data").path("schema").path("$ref").asText();
        assertThat(document.path("components").path("schemas").path(ref.substring(ref.lastIndexOf('/')+1)).path("properties").path("file").path("format").asText()).isEqualTo("binary");
        assertThat(paths.path("/api/v1/drive/connections/{slot}/files/{fileId}/download").path("get").path("responses").path("200").path("content").path("application/octet-stream").path("schema").path("format").asText()).isEqualTo("binary");
        assertThat(paths.path("/api/v1/drive/oauth/callback").path("get").path("security")).isEmpty();
        assertThat(send(HttpRequest.newBuilder(uri("/api/v1/drive/oauth/callback")).GET()).statusCode()).isEqualTo(400);
        assertThat(send(HttpRequest.newBuilder(uri("/api/v1/drive/oauth/callback")).POST(HttpRequest.BodyPublishers.noBody())).statusCode()).isEqualTo(401);
        assertThat(GOOGLE.calls).isEmpty();
    }
    @Test void generatedJsonSuccessSchemasDescribeTheActualRootPageAndFileEnvelopes() throws Exception {
        var root=send(request(path(1)+"/root-folder").POST(HttpRequest.BodyPublishers.noBody()));
        String action=key();byte[] bytes={1,2,3};
        var created=upload(1,action,bytes,"schema.bin","application/octet-stream");
        var repeated=upload(1,action,bytes,"schema.bin","application/octet-stream");
        var page=send(request(path(1)+"/files").GET());
        var response=send(HttpRequest.newBuilder(uri("/v3/api-docs")).GET());assertThat(response.statusCode()).isEqualTo(200);
        var document=mapper.readTree(response.body());var paths=document.path("paths");
        var rootProperties=jsonSuccessProperties(document,paths.path("/api/v1/drive/connections/{slot}/root-folder").path("post"),200,root);
        assertThat(rootProperties.fieldNames()).toIterable().containsExactlyInAnyOrder("slot","status");
        assertThat(rootProperties.path("slot").path("type").asText()).isEqualTo("integer");
        assertThat(rootProperties.path("status").path("type").asText()).isEqualTo("string");
        var files=paths.path("/api/v1/drive/connections/{slot}/files");
        var pageProperties=jsonSuccessProperties(document,files.path("get"),200,page);
        assertThat(pageProperties.fieldNames()).toIterable().containsExactlyInAnyOrder("files","nextCursor","hasMore");
        assertThat(pageProperties.path("nextCursor").path("type").asText()).isEqualTo("string");
        assertThat(pageProperties.path("hasMore").path("type").asText()).isEqualTo("boolean");
        var array=resolveSchema(document,pageProperties.path("files"));assertThat(array.path("type").asText()).isEqualTo("array");
        fileProperties(schemaProperties(document,array.path("items")));
        fileProperties(jsonSuccessProperties(document,files.path("post"),201,created));
        fileProperties(jsonSuccessProperties(document,files.path("post"),200,repeated));
    }
    private ObjectNode jsonSuccessProperties(JsonNode document,JsonNode operation,int status,HttpResponse<byte[]> wire) throws Exception {
        assertThat(wire.statusCode()).isEqualTo(status);
        assertThat(wire.headers().firstValue("Content-Type").orElseThrow()).startsWith("application/json");
        assertThat(operation.path("security").toString()).contains("bearerAuth");
        var content=operation.path("responses").path(Integer.toString(status)).path("content");
        assertThat(content.fieldNames()).toIterable().containsExactly("application/json");
        var envelope=schemaProperties(document,content.path("application/json").path("schema"));
        // Exact public fields exclude callable/timeout/executor/beanFactory and other async internals.
        assertThat(envelope.fieldNames()).toIterable().containsExactlyInAnyOrder("data","error","meta");
        var payload=mapper.readTree(wire.body());
        assertThat(payload.fieldNames()).toIterable().containsExactlyInAnyOrderElementsOf(() -> envelope.fieldNames());
        assertThat(envelope.path("error").path("$ref").asText()).isEqualTo("#/components/schemas/ApiError");
        assertThat(envelope.path("meta").path("$ref").asText()).isEqualTo("#/components/schemas/ApiMeta");
        var data=schemaProperties(document,envelope.path("data"));
        assertThat(payload.path("data").fieldNames()).toIterable().containsExactlyInAnyOrderElementsOf(() -> data.fieldNames());
        return data;
    }
    private void fileProperties(ObjectNode properties) {
        assertThat(properties.fieldNames()).toIterable().containsExactlyInAnyOrder("id","name","mimeType","sizeBytes","modifiedAt");
        for(String field:List.of("id","name","mimeType","modifiedAt"))assertThat(properties.path(field).path("type").asText()).isEqualTo("string");
        assertThat(properties.path("sizeBytes").path("type").asText()).isEqualTo("integer");
        assertThat(properties.path("modifiedAt").path("format").asText()).isEqualTo("date-time");
    }
    private ObjectNode schemaProperties(JsonNode document,JsonNode schema) {
        schema=resolveSchema(document,schema);var properties=mapper.createObjectNode();
        for(var part:schema.path("allOf"))properties.setAll(schemaProperties(document,part));
        if(schema.has("properties"))properties.setAll((ObjectNode)schema.path("properties"));
        assertThat(properties.isEmpty()).as("Resolved public schema properties").isFalse();return properties;
    }
    private JsonNode resolveSchema(JsonNode document,JsonNode schema) {
        var seen=new HashSet<String>();
        while(schema.has("$ref")){
            String ref=schema.path("$ref").asText();assertThat(ref).startsWith("#/components/schemas/");
            assertThat(seen.add(ref)).as("Acyclic schema reference").isTrue();schema=document.at(ref.substring(1));
            assertThat(schema.isMissingNode()).as("Resolvable schema reference %s",ref).isFalse();
        }
        return schema;
    }
}
