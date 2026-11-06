package com.vhvkhangg.personalprivatevault.drive.internal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ppv.drive")
@Getter
@Setter
public class DriveProperties {

    public static final String LOCAL_CALLBACK = "http://localhost:8080/api/v1/drive/oauth/callback";
    public static final String AUTHORIZATION_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth";
    public static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
    public static final String JWKS_URI = "https://www.googleapis.com/oauth2/v3/certs";
    public static final String REVOKE_ENDPOINT = "https://oauth2.googleapis.com/revoke";

    private boolean enabled = false;
    private String tokenKeyFile;
    private OAuth oauth = new OAuth();

    @Getter
    @Setter
    public static class OAuth {
        private String clientId = "";
        private String clientSecret = "";
        private String redirectUri = LOCAL_CALLBACK;
        // Retained as bound values solely to reject unapproved overrides at startup.
        private String authorizationEndpoint = AUTHORIZATION_ENDPOINT;
        private String tokenEndpoint = TOKEN_ENDPOINT;
        private String jwksUri = JWKS_URI;
        private String revokeEndpoint = REVOKE_ENDPOINT;
    }
}
