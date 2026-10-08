package com.vhvkhangg.personalprivatevault;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
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
            "uploadImage"
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
                                .description("JWT Bearer access token authorization.")))
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
                        if (pathKey.equals("/api/v1/auth/login") || pathKey.equals("/api/v1/auth/refresh")) {
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
                });
            });
        };
    }
}
