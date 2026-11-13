package com.vhvkhangg.personalprivatevault.film.internal.web.controller;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;
import com.vhvkhangg.personalprivatevault.film.enums.FilmFormat;
import com.vhvkhangg.personalprivatevault.film.enums.FilmProductionStyle;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.CreateFilmRequest;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.PublicCreateFilmRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * Profile-aware reader for film creation requests.
 * Enforces strict original-byte duplicate detection, property naming, root type, and type constraints for PUBLIC,
 * while delegating to configured MVC Jackson converter and Bean Validation for PRIVATE.
 */
@Component
@RequiredArgsConstructor
public class FilmCreateRequestReader {

    public static class MalformedPayloadException extends RuntimeException {
        public MalformedPayloadException(String message) {
            super(message);
        }

        public MalformedPayloadException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static final Set<String> ALLOWED_PUBLIC_FIELDS = Set.of(
            "title",
            "originalTitle",
            "nationalityCode",
            "format",
            "productionStyle"
    );

    private static final ObjectMapper PUBLIC_STRICT_MAPPER = createPublicStrictMapper();

    private static ObjectMapper createPublicStrictMapper() {
        ObjectMapper mapper = new ObjectMapper()
                .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS);
        mapper.coercionConfigFor(LogicalType.Textual)
                .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Array, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Object, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Binary, CoercionAction.Fail);
        mapper.coercionConfigFor(LogicalType.Enum)
                .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Array, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Object, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Binary, CoercionAction.Fail);
        return mapper;
    }

    private final RequestMappingHandlerAdapter handlerAdapter;
    private final Validator validator;

    public PublicCreateFilmRequest readPublic(byte[] body) {
        try {
            return readPublic(body, null);
        } catch (HttpMediaTypeNotSupportedException e) {
            throw new MalformedPayloadException("Unsupported media type: " + e.getMessage(), e);
        }
    }

    public PublicCreateFilmRequest readPublic(byte[] body, HttpServletRequest servletRequest) throws HttpMediaTypeNotSupportedException {
        if (servletRequest != null) {
            String contentTypeHeader = servletRequest.getContentType();
            if (contentTypeHeader == null || contentTypeHeader.isBlank()) {
                throw new HttpMediaTypeNotSupportedException(
                        (MediaType) null,
                        List.of(MediaType.APPLICATION_JSON, MediaType.parseMediaType("application/*+json"))
                );
            }

            MediaType mediaType;
            try {
                mediaType = MediaType.parseMediaType(contentTypeHeader);
            } catch (IllegalArgumentException e) {
                throw new HttpMediaTypeNotSupportedException(
                        (MediaType) null,
                        List.of(MediaType.APPLICATION_JSON, MediaType.parseMediaType("application/*+json"))
                );
            }

            if (!isSupportedJsonMediaType(mediaType)) {
                throw new HttpMediaTypeNotSupportedException(
                        mediaType,
                        List.of(MediaType.APPLICATION_JSON, MediaType.parseMediaType("application/*+json"))
                );
            }
        }

        if (body == null || body.length == 0) {
            throw new MalformedPayloadException("Request body must not be empty");
        }
        boolean hasNonWhitespace = false;
        for (byte b : body) {
            if (!Character.isWhitespace((char) b)) {
                hasNonWhitespace = true;
                break;
            }
        }
        if (!hasNonWhitespace) {
            throw new MalformedPayloadException("Request body must not be empty");
        }

        JsonNode tree;
        try (JsonParser parser = PUBLIC_STRICT_MAPPER.createParser(body)) {
            tree = PUBLIC_STRICT_MAPPER.readTree(parser);
            if (tree == null || !tree.isObject()) {
                throw new MalformedPayloadException("Request body must be a JSON object");
            }
            JsonToken trailingToken = parser.nextToken();
            if (trailingToken != null) {
                throw new MalformedPayloadException("Trailing token found after JSON object");
            }
        } catch (IOException e) {
            throw new MalformedPayloadException("JSON syntax error: " + e.getMessage(), e);
        }

        var fieldNames = tree.fieldNames();
        while (fieldNames.hasNext()) {
            String field = fieldNames.next();
            if (!ALLOWED_PUBLIC_FIELDS.contains(field)) {
                throw new MalformedPayloadException("Unrecognized or forbidden property: " + field);
            }
        }

        validatePublicFieldTypes(tree);

        String title = tree.has("title") && !tree.get("title").isNull() ? tree.get("title").asText() : null;
        String originalTitle = tree.has("originalTitle") && !tree.get("originalTitle").isNull() ? tree.get("originalTitle").asText() : null;
        String nationalityCode = tree.has("nationalityCode") && !tree.get("nationalityCode").isNull() ? tree.get("nationalityCode").asText() : null;
        FilmFormat format = null;
        if (tree.has("format") && !tree.get("format").isNull()) {
            String formatStr = tree.get("format").asText();
            try {
                format = FilmFormat.valueOf(formatStr);
            } catch (IllegalArgumentException e) {
                throw new MalformedPayloadException("Invalid format enum: " + formatStr);
            }
        }
        FilmProductionStyle productionStyle = null;
        if (tree.has("productionStyle") && !tree.get("productionStyle").isNull()) {
            String styleStr = tree.get("productionStyle").asText();
            try {
                productionStyle = FilmProductionStyle.valueOf(styleStr);
            } catch (IllegalArgumentException e) {
                throw new MalformedPayloadException("Invalid production style enum: " + styleStr);
            }
        }

        PublicCreateFilmRequest request = new PublicCreateFilmRequest(title, originalTitle, nationalityCode, format, productionStyle);

        Set<ConstraintViolation<PublicCreateFilmRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        return request;
    }

    private void validatePublicFieldTypes(JsonNode tree) {
        if (tree.has("title")) {
            JsonNode node = tree.get("title");
            if (!node.isNull() && !node.isTextual()) {
                throw new MalformedPayloadException("Field 'title' must be a string");
            }
        }
        if (tree.has("originalTitle")) {
            JsonNode node = tree.get("originalTitle");
            if (!node.isNull() && !node.isTextual()) {
                throw new MalformedPayloadException("Field 'originalTitle' must be a string");
            }
        }
        if (tree.has("nationalityCode")) {
            JsonNode node = tree.get("nationalityCode");
            if (!node.isNull() && !node.isTextual()) {
                throw new MalformedPayloadException("Field 'nationalityCode' must be a string");
            }
        }
        if (tree.has("format")) {
            JsonNode node = tree.get("format");
            if (!node.isNull()) {
                if (!node.isTextual()) {
                    throw new MalformedPayloadException("Field 'format' must be a string");
                }
                String text = node.asText();
                if (!"MOVIE".equals(text) && !"SERIES".equals(text)) {
                    throw new MalformedPayloadException("Unknown format enum: " + text);
                }
            }
        }
        if (tree.has("productionStyle")) {
            JsonNode node = tree.get("productionStyle");
            if (!node.isNull()) {
                if (!node.isTextual()) {
                    throw new MalformedPayloadException("Field 'productionStyle' must be a string");
                }
                String text = node.asText();
                if (!"LIVE_ACTION".equals(text) && !"ANIMATION".equals(text)) {
                    throw new MalformedPayloadException("Unknown production style enum: " + text);
                }
            }
        }
    }

    private static boolean isSupportedJsonMediaType(MediaType mediaType) {
        if (mediaType == null) {
            return false;
        }
        if (mediaType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
            return true;
        }
        return "application".equalsIgnoreCase(mediaType.getType())
                && mediaType.getSubtype().toLowerCase().endsWith("+json");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public CreateFilmRequest readPrivate(byte[] body, HttpServletRequest servletRequest) throws IOException, HttpMediaTypeNotSupportedException {
        if (body == null || body.length == 0) {
            throw new HttpMessageNotReadableException(
                    "Required request body is missing",
                    (HttpInputMessage) null
            );
        }

        String contentTypeHeader = servletRequest.getContentType();
        if (contentTypeHeader == null || contentTypeHeader.isBlank()) {
            throw new HttpMediaTypeNotSupportedException(
                    (MediaType) null,
                    List.of(MediaType.APPLICATION_JSON, MediaType.parseMediaType("application/*+json"))
            );
        }

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(contentTypeHeader);
        } catch (IllegalArgumentException e) {
            throw new HttpMediaTypeNotSupportedException(
                    (MediaType) null,
                    List.of(MediaType.APPLICATION_JSON, MediaType.parseMediaType("application/*+json"))
            );
        }

        HttpHeaders headers = new ServletServerHttpRequest(servletRequest).getHeaders();
        HttpInputMessage inputMessage = new SimpleHttpInputMessage(body, headers);

        for (HttpMessageConverter converter : handlerAdapter.getMessageConverters()) {
            if (converter.canRead(CreateFilmRequest.class, mediaType)) {
                CreateFilmRequest request = (CreateFilmRequest) converter.read(CreateFilmRequest.class, inputMessage);
                if (request == null) {
                    throw new HttpMessageNotReadableException(
                            "Required request body is missing",
                            inputMessage
                    );
                }
                Set<ConstraintViolation<CreateFilmRequest>> violations = validator.validate(request);
                if (!violations.isEmpty()) {
                    throw new ConstraintViolationException(violations);
                }
                return request;
            }
        }
        throw new HttpMediaTypeNotSupportedException(mediaType, List.of(MediaType.APPLICATION_JSON));
    }

    private static final class SimpleHttpInputMessage implements HttpInputMessage {
        private final ByteArrayInputStream inputStream;
        private final HttpHeaders headers;

        SimpleHttpInputMessage(byte[] body, HttpHeaders headers) {
            this.inputStream = new ByteArrayInputStream(body);
            this.headers = headers;
        }

        @Override
        public InputStream getBody() {
            return inputStream;
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }
    }
}
