package com.vhvkhangg.personalprivatevault;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Set;

/**
 * Global OpenAPI/Swagger documentation configuration for the Personal Private Vault REST API.
 */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {

    public static final String BEARER_SECURITY_SCHEME = "bearerAuth";

    private static final Set<String> PUBLIC_AUTH_PATHS = Set.of(
            "/api/v1/auth/bootstrap/status",
            "/api/v1/auth/bootstrap",
            "/api/v1/auth/login",
            "/api/v1/auth/profiles/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/revoke"
    );

    private static final Set<String> CREATION_OPERATION_IDS = Set.of(
            "bootstrapVault",
            "createPerson",
            "createCreatorGroup",
            "createFiction",
            "createFictionLink",
            "createFictionGenre",
            "createFilm",
            "createFilmCredit",
            "createFilmLink",
            "createFilmGenre",
            "createAlbum",
            "createImage",
            "createLocation",
            "createLocationCategory",
            "createBrand",
            "createAddress",
            "createStudyItem",
            "createInformationItem",
            "createVocabularyItem",
            "createNote",
            "createCollectionMusic",
            "createCollectionShoppingItem",
            "createCollectionSoftwareItem",
            "createExternalAccount",
            "createFollowerSnapshot",
            "createFeedSource",
            "saveFeedItemResource",
            "saveManualResource",
            "convertSavedResourceToStudy",
            "convertSavedResourceToInformation",
            "convertSavedResourceToNote",
            "createImportJob",
            "createWallet",
            "createTransactionCategory",
            "createFinancialTransaction",
            "createRecurringRule",
            "createSubscription",
            "createDiaryEntry",
            "createPersonalProfile",
            "createTag",
            "uploadImage",
            "registerFileCandidate",
            "registerDomainCandidate",
            "admitRagJob",
            "admitRagDocumentBatch"
    );

    @Bean
    public OpenAPI personalPrivateVaultOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Personal Private Vault API")
                        .version("v1")
                        .description("REST API for Personal Private Vault modular monolith."))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SECURITY_SCHEME, new SecurityScheme()
                                .name(BEARER_SECURITY_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("DP-A persisted session authority: sub=1, profile PUBLIC or PRIVATE, sid and positive integral generation. Anonymous endpoints permit unauthenticated access; authenticated PROFILE_PUBLIC grants access to auth session elevation, POST /api/v1/films, GET /api/v1/fictions/{id}, GET /api/v1/films/{id}, and GET /api/v1/reference/{countries,languages,currencies} (list and detail); session downgrade and all other business and data operations require PROFILE_PRIVATE.")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SECURITY_SCHEME));
    }

    @Bean
    public OpenApiCustomizer personalPrivateVaultOpenApiCustomizer() {
        Schema<Object> apiFieldErrorSchema = new Schema<>()
                .type("object")
                .description("Field validation error")
                .addProperty("field", new Schema<String>().type("string").example("email"))
                .addProperty("message", new Schema<String>().type("string").example("must be a well-formed email address"));

        Schema<Object> apiErrorSchema = new Schema<>()
                .type("object")
                .description("Standard API error payload")
                .addProperty("code", new Schema<String>().type("string").example("VALIDATION_ERROR"))
                .addProperty("message", new Schema<String>().type("string").example("Request validation failed"))
                .addProperty("fieldErrors", new Schema<Object>().type("array").items(new Schema<Object>().$ref("#/components/schemas/ApiFieldError")));

        Schema<Object> errorEnvelopeSchema = new Schema<>()
                .type("object")
                .description("Standard API error response envelope")
                .addProperty("data", new Schema<Object>().nullable(true).example(null))
                .addProperty("error", new Schema<Object>().$ref("#/components/schemas/ApiError"))
                .addProperty("meta", new Schema<Object>().nullable(true).example(null));

        Content errorContent = new Content().addMediaType(
                org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                new MediaType().schema(new Schema<Object>().$ref("#/components/schemas/ErrorResponse"))
        );

        return openApi -> {
            if (openApi.getComponents() == null) {
                openApi.setComponents(new Components());
            }
            openApi.getComponents().addSchemas("ApiFieldError", apiFieldErrorSchema);
            openApi.getComponents().addSchemas("ApiError", apiErrorSchema);
            openApi.getComponents().addSchemas("ErrorResponse", errorEnvelopeSchema);

            Schema<Object> publicFictionResponseSchema = new ObjectSchema()
                    .description("Public fiction work detail")
                    .addProperty("id", new IntegerSchema().format("int64").example(1L).description("Fiction ID (identical to underlying vault entry ID)"))
                    .addProperty("title", new StringSchema().example("Sample Fiction").description("Primary title"))
                    .addProperty("originalTitle", new StringSchema().types(Set.of("string", "null")).nullable(true).example(null).description("Optional original/native title"))
                    .addProperty("nationalityCode", new StringSchema().types(Set.of("string", "null")).nullable(true).example("VN").description("Optional 2-character country code"))
                    .addProperty("format", new StringSchema()._enum(java.util.List.of("NOVEL", "COMIC")).example("NOVEL").description("Format of the work (NOVEL or COMIC)"))
                    .addProperty("isNsfw", new BooleanSchema()._enum(java.util.List.of(false)).example(false).description("Whether the work is marked NSFW (always false on public success)"))
                    .required(java.util.List.of("id", "title", "originalTitle", "nationalityCode", "format", "isNsfw"))
                    .additionalProperties(false);

            Schema<Object> apiResponsePublicFictionSchema = new ObjectSchema()
                    .description("Canonical envelope for public fiction work detail")
                    .addProperty("data", new Schema<Object>().$ref("#/components/schemas/PublicFictionResponse"))
                    .addProperty("error", new Schema<Object>().$ref("#/components/schemas/ApiError").nullable(true).example(null))
                    .addProperty("meta", new Schema<Object>().nullable(true).example(null));

            Schema<Object> fictionDetailResponseSchema = new ObjectSchema()
                    .description("Fiction work detail response envelope: ApiResponsePublicFictionResponse for PROFILE_PUBLIC, ApiResponseFictionResponse for PROFILE_PRIVATE")
                    .anyOf(java.util.List.of(
                            new Schema<Object>().$ref("#/components/schemas/ApiResponsePublicFictionResponse"),
                            new Schema<Object>().$ref("#/components/schemas/ApiResponseFictionResponse")
                    ));

            Schema<Object> publicFilmResponseSchema = new ObjectSchema()
                    .description("Public film work detail")
                    .addProperty("id", new IntegerSchema().format("int64").example(1L).description("Film ID (identical to underlying vault entry ID)"))
                    .addProperty("title", new StringSchema().example("Sample Film").description("Primary title"))
                    .addProperty("originalTitle", new StringSchema().types(Set.of("string", "null")).nullable(true).example(null).description("Optional original/native title"))
                    .addProperty("nationalityCode", new StringSchema().types(Set.of("string", "null")).nullable(true).example("VN").description("Optional 2-character country code"))
                    .addProperty("format", new StringSchema()._enum(java.util.List.of("MOVIE", "SERIES")).example("MOVIE").description("Format of the work (MOVIE or SERIES)"))
                    .addProperty("productionStyle", new StringSchema()._enum(java.util.List.of("LIVE_ACTION", "ANIMATION")).example("LIVE_ACTION").description("Production style (LIVE_ACTION or ANIMATION)"))
                    .addProperty("isNsfw", new BooleanSchema()._enum(java.util.List.of(false)).example(false).description("Whether the work is marked NSFW (always false on public success)"))
                    .required(java.util.List.of("id", "title", "originalTitle", "nationalityCode", "format", "productionStyle", "isNsfw"))
                    .additionalProperties(false);

            Schema<Object> apiResponsePublicFilmSchema = new ObjectSchema()
                    .description("Canonical envelope for public film work detail")
                    .addProperty("data", new Schema<Object>().$ref("#/components/schemas/PublicFilmResponse"))
                    .addProperty("error", new Schema<Object>().$ref("#/components/schemas/ApiError").nullable(true).example(null))
                    .addProperty("meta", new Schema<Object>().nullable(true).example(null));

            Schema<Object> filmDetailResponseSchema = new ObjectSchema()
                    .description("Film work detail response envelope: ApiResponsePublicFilmResponse for PROFILE_PUBLIC, ApiResponseFilmResponse for PROFILE_PRIVATE")
                    .anyOf(java.util.List.of(
                            new Schema<Object>().$ref("#/components/schemas/ApiResponsePublicFilmResponse"),
                            new Schema<Object>().$ref("#/components/schemas/ApiResponseFilmResponse")
                    ));

            openApi.getComponents().addSchemas("PublicFictionResponse", publicFictionResponseSchema);
            openApi.getComponents().addSchemas("ApiResponsePublicFictionResponse", apiResponsePublicFictionSchema);
            openApi.getComponents().addSchemas("ApiResponseFictionDetailResponse", fictionDetailResponseSchema);

            openApi.getComponents().addSchemas("PublicFilmResponse", publicFilmResponseSchema);
            openApi.getComponents().addSchemas("ApiResponsePublicFilmResponse", apiResponsePublicFilmSchema);
            openApi.getComponents().addSchemas("ApiResponseFilmDetailResponse", filmDetailResponseSchema);

            Schema<Object> publicCreateFilmRequestSchema = new ObjectSchema()
                    .description("Public film creation request payload (strictly 5 properties, no client-supplied isNsfw, relational, or consumption fields)")
                    .addProperty("title", new StringSchema().maxLength(500).example("Sample Film").description("Primary title"))
                    .addProperty("originalTitle", new StringSchema().maxLength(500).types(Set.of("string", "null")).nullable(true).example(null).description("Optional original/native title"))
                    .addProperty("nationalityCode", new StringSchema().maxLength(2).types(Set.of("string", "null")).nullable(true).example("VN").description("Optional 2-character country code"))
                    .addProperty("format", new StringSchema()._enum(java.util.List.of("MOVIE", "SERIES")).example("MOVIE").description("Format of the work (MOVIE or SERIES)"))
                    .addProperty("productionStyle", new StringSchema()._enum(java.util.List.of("LIVE_ACTION", "ANIMATION")).example("LIVE_ACTION").description("Production style (LIVE_ACTION or ANIMATION)"))
                    .required(java.util.List.of("title", "format", "productionStyle"))
                    .additionalProperties(false);

            try {
                Class<?> clazz = Class.forName("com.vhvkhangg.personalprivatevault.film.internal.web.dto.CreateFilmRequest");
                java.util.Map<String, Schema> createFilmSchemas = io.swagger.v3.core.converter.ModelConverters.getInstance().readAll(clazz);
                createFilmSchemas.forEach((k, v) -> openApi.getComponents().addSchemas(k, v));
            } catch (ClassNotFoundException ignored) {
            }

            Schema<Object> filmCreateRequestSchema = new ObjectSchema()
                    .description("Film creation request: PublicCreateFilmRequest for PROFILE_PUBLIC, CreateFilmRequest for PROFILE_PRIVATE")
                    .anyOf(java.util.List.of(
                            new Schema<Object>().$ref("#/components/schemas/PublicCreateFilmRequest"),
                            new Schema<Object>().$ref("#/components/schemas/CreateFilmRequest")
                    ));

            Schema<Object> filmCreateResponseSchema = new ObjectSchema()
                    .description("Film creation response envelope: ApiResponsePublicFilmResponse for PROFILE_PUBLIC, ApiResponseFilmResponse for PROFILE_PRIVATE")
                    .anyOf(java.util.List.of(
                            new Schema<Object>().$ref("#/components/schemas/ApiResponsePublicFilmResponse"),
                            new Schema<Object>().$ref("#/components/schemas/ApiResponseFilmResponse")
                    ));

            openApi.getComponents().addSchemas("PublicCreateFilmRequest", publicCreateFilmRequestSchema);
            openApi.getComponents().addSchemas("FilmCreateRequest", filmCreateRequestSchema);
            openApi.getComponents().addSchemas("ApiResponseFilmCreateResponse", filmCreateResponseSchema);

            // DP-A and DP-B2 route-local parsers reject unknown members in these request objects.
            for (String name : Set.of("PublicProfileLoginRequest", "PublicCredentialRequest", "ElevatePrivateRequest", "PublicCreateFilmRequest")) {
                Schema<?> schema = openApi.getComponents().getSchemas().get(name);
                if (schema != null) schema.setAdditionalProperties(false);
            }

            if (openApi.getPaths() == null) {
                return;
            }

            openApi.getPaths().forEach((pathKey, pathItem) -> {
                boolean isPublic = PUBLIC_AUTH_PATHS.contains(pathKey);

                pathItem.readOperationsMap().forEach((httpMethod, operation) -> {
                    // 1. Explicitly anonymous security for public endpoints
                    if (isPublic) {
                        operation.setSecurity(Collections.emptyList());
                    }

                    ApiResponses responses = operation.getResponses();
                    if (responses == null) {
                        responses = new ApiResponses();
                        operation.setResponses(responses);
                    }

                    // D4 keeps the callback's fixed redirect and disables token issuance via Google.
                    if (pathKey.equals("/api/v1/auth/google/callback")) {
                        operation.setSecurity(Collections.emptyList());
                        responses.clear();
                        responses.addApiResponse("303", new ApiResponse()
                                .description("D4: LINK completion or failed LOGIN redirects to the fixed BFF completion URL; no token response")
                                .addHeaderObject("Location", new io.swagger.v3.oas.models.headers.Header()
                                        .schema(new Schema<String>().type("string"))));
                        return;
                    }

                    String operationId = operation.getOperationId();
                    boolean isCreation = operationId != null && CREATION_OPERATION_IDS.contains(operationId);

                    // 2. Accurate 201 Created status for creation operations
                    if (isCreation) {
                        if (responses.containsKey("200") && !responses.containsKey("201")) {
                            ApiResponse resp200 = responses.remove("200");
                            resp200.setDescription("Resource created successfully");
                            responses.addApiResponse("201", resp200);
                        }
                    }

                    // 3. Relevant error responses
                    if (!responses.containsKey("400")) {
                        responses.addApiResponse("400", new ApiResponse()
                                .description("Bad Request - Malformed request body or invalid parameters")
                                .content(errorContent));
                    }

                    if (isPublic) {
                        if (pathKey.equals("/api/v1/auth/login") || pathKey.equals("/api/v1/auth/refresh") || pathKey.equals("/api/v1/auth/profiles/login")) {
                            if (!responses.containsKey("401")) {
                                responses.addApiResponse("401", new ApiResponse()
                                        .description("Unauthorized - Invalid credentials or token")
                                        .content(errorContent));
                            }
                        }
                        if (pathKey.equals("/api/v1/auth/bootstrap")) {
                            if (!responses.containsKey("409")) {
                                responses.addApiResponse("409", new ApiResponse()
                                        .description("Conflict - Singleton vault is already bootstrapped")
                                        .content(errorContent));
                            }
                            if (!responses.containsKey("422")) {
                                responses.addApiResponse("422", new ApiResponse()
                                        .description("Unprocessable Content - Invalid bootstrap parameters")
                                        .content(errorContent));
                            }
                        }
                    } else {
                        // Protected endpoints
                        if (!responses.containsKey("401")) {
                            responses.addApiResponse("401", new ApiResponse()
                                    .description("Unauthorized - Missing, invalid, or expired Bearer token")
                                    .content(errorContent));
                        }
                        if (!responses.containsKey("403")) {
                            responses.addApiResponse("403", new ApiResponse()
                                    .description("Forbidden - Access denied")
                                    .content(errorContent));
                        }
                    }

                    if (pathKey.contains("{") || (httpMethod == io.swagger.v3.oas.models.PathItem.HttpMethod.GET && !pathKey.endsWith("/status"))) {
                        if (!responses.containsKey("404")) {
                            responses.addApiResponse("404", new ApiResponse()
                                    .description("Not Found - Requested resource does not exist")
                                    .content(errorContent));
                        }
                    }

                    if (pathKey.equals("/api/v1/images/{id}/content")) {
                        if (!responses.containsKey("409")) {
                            responses.addApiResponse("409", new ApiResponse()
                                    .description("Conflict - Storage integrity error (metadata exists but object missing)")
                                    .content(errorContent));
                        }
                    }

                    if (pathKey.equals("/api/v1/images/upload")) {
                        ApiResponse resp400 = responses.get("400");
                        if (resp400 != null) {
                            resp400.setContent(errorContent);
                        } else {
                            responses.addApiResponse("400", new ApiResponse()
                                    .description("Bad Request - Malformed multipart request or invalid parameters")
                                    .content(errorContent));
                        }
                        ApiResponse resp413 = responses.get("413");
                        if (resp413 != null) {
                            resp413.setContent(errorContent);
                        } else {
                            responses.addApiResponse("413", new ApiResponse()
                                    .description("Payload Too Large - File exceeds maximum configured upload size limit")
                                    .content(errorContent));
                        }
                    }

                    boolean isMutation = (httpMethod == io.swagger.v3.oas.models.PathItem.HttpMethod.POST
                            || httpMethod == io.swagger.v3.oas.models.PathItem.HttpMethod.PUT
                            || httpMethod == io.swagger.v3.oas.models.PathItem.HttpMethod.DELETE
                            || httpMethod == io.swagger.v3.oas.models.PathItem.HttpMethod.PATCH)
                            && !pathKey.equals("/api/v1/auth/login")
                            && !pathKey.equals("/api/v1/auth/refresh")
                            && !pathKey.equals("/api/v1/auth/revoke")
                            && !pathKey.equals("/api/v1/auth/profiles/login")
                            && !pathKey.equals("/api/v1/auth/private-pin/verify")
                            && !pathKey.equals("/api/v1/portability/exports");

                    if (isMutation) {
                        if (!responses.containsKey("409")) {
                            responses.addApiResponse("409", new ApiResponse()
                                    .description("Conflict - Unique constraint or state conflict")
                                    .content(errorContent));
                        }
                        if (!responses.containsKey("422")) {
                            responses.addApiResponse("422", new ApiResponse()
                                    .description("Unprocessable Content - Business invariant violation")
                                    .content(errorContent));
                        }
                    }

                    boolean hasRequestBody = operation.getRequestBody() != null
                            || httpMethod == io.swagger.v3.oas.models.PathItem.HttpMethod.POST
                            || httpMethod == io.swagger.v3.oas.models.PathItem.HttpMethod.PUT
                            || httpMethod == io.swagger.v3.oas.models.PathItem.HttpMethod.PATCH;
                    if (hasRequestBody && !responses.containsKey("415")) {
                        responses.addApiResponse("415", new ApiResponse()
                                .description("Unsupported Media Type - Content-Type is not supported")
                                .content(errorContent));
                    }

                    if (!responses.containsKey("406")) {
                        responses.addApiResponse("406", new ApiResponse()
                                .description("Not Acceptable - Acceptable representation cannot be produced")
                                .content(errorContent));
                    }

                    if (!responses.containsKey("500")) {
                        responses.addApiResponse("500", new ApiResponse()
                                .description("Internal Server Error - Unexpected server failure")
                                .content(errorContent));
                    }
                    if (pathKey.equals("/api/v1/auth/profiles/login")) {
                        responses.get("401").setDescription("AUTH_INVALID_CREDENTIALS - wrong or unconfigured PUBLIC password");
                    } else if (pathKey.equals("/api/v1/auth/profiles/public-credential")) {
                        responses.get("422").setDescription("AUTH_PASSWORD_NOT_DISTINCT - PUBLIC password must differ from PRIVATE password");
                    } else if (pathKey.equals("/api/v1/auth/sessions/downgrade-public")) {
                        responses.get("409").setDescription("AUTH_PUBLIC_NOT_CONFIGURED - PUBLIC credential is not configured");
                    } else if (pathKey.equals("/api/v1/auth/google/start") || pathKey.equals("/api/v1/auth/google/handoff")) {
                        operation.setSecurity(Collections.emptyList());
                        responses.remove("200");
                        responses.remove("401");
                        responses.remove("403");
                        responses.addApiResponse("503", new ApiResponse().description("GOOGLE_SIGNIN_UNAVAILABLE - DP-A has no safe profile selection or issuance mapping").content(errorContent));
                    } else if (pathKey.equals("/api/v1/fictions/{id}") && httpMethod == io.swagger.v3.oas.models.PathItem.HttpMethod.GET) {
                        ApiResponse resp200 = responses.get("200");
                        if (resp200 != null) {
                            resp200.setDescription("Fiction work detail (minimal for PROFILE_PUBLIC, full for PROFILE_PRIVATE; Cache-Control: no-store for PROFILE_PUBLIC)");
                            MediaType mediaType = new MediaType().schema(new Schema<Object>().$ref("#/components/schemas/ApiResponseFictionDetailResponse"));
                            resp200.setContent(new Content()
                                    .addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE, mediaType)
                                    .addMediaType("*/*", mediaType));
                        }
                    } else if (pathKey.equals("/api/v1/films/{id}") && httpMethod == io.swagger.v3.oas.models.PathItem.HttpMethod.GET) {
                        ApiResponse resp200 = responses.get("200");
                        if (resp200 != null) {
                            resp200.setDescription("Film work detail (minimal for PROFILE_PUBLIC, full for PROFILE_PRIVATE; Cache-Control: no-store for PROFILE_PUBLIC)");
                            MediaType mediaType = new MediaType().schema(new Schema<Object>().$ref("#/components/schemas/ApiResponseFilmDetailResponse"));
                            resp200.setContent(new Content()
                                    .addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE, mediaType)
                                    .addMediaType("*/*", mediaType));
                        }
                    } else if (pathKey.equals("/api/v1/films") && httpMethod == io.swagger.v3.oas.models.PathItem.HttpMethod.POST) {
                        io.swagger.v3.oas.models.parameters.RequestBody reqBody = operation.getRequestBody();
                        if (reqBody == null) {
                            reqBody = new io.swagger.v3.oas.models.parameters.RequestBody();
                            operation.setRequestBody(reqBody);
                        }
                        reqBody.setRequired(true);
                        MediaType mediaType = new MediaType().schema(new Schema<Object>().$ref("#/components/schemas/FilmCreateRequest"));
                        reqBody.setContent(new Content()
                                .addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE, mediaType)
                                .addMediaType("*/*", mediaType));
                        reqBody.setDescription("Film creation request: PublicCreateFilmRequest for PROFILE_PUBLIC, CreateFilmRequest for PROFILE_PRIVATE");

                        ApiResponse resp201 = responses.get("201");
                        if (resp201 != null) {
                            resp201.setDescription("Film work created (minimal PublicFilmResponse with Cache-Control: no-store for PROFILE_PUBLIC, full FilmResponse for PROFILE_PRIVATE)");
                            MediaType respMediaType = new MediaType().schema(new Schema<Object>().$ref("#/components/schemas/ApiResponseFilmCreateResponse"));
                            resp201.setContent(new Content()
                                    .addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE, respMediaType)
                                    .addMediaType("*/*", respMediaType));
                        }
                    }
                });
            });
        };
    }
}
