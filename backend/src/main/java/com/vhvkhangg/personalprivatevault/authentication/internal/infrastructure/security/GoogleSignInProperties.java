package com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "ppv.security.google-signin")
public class GoogleSignInProperties {

    private boolean enabled = false;
    private String clientId = "";
    private String clientSecret = "";
    private String txnKeyFile = "";

    public void validate() {
        if (!enabled) {
            return;
        }
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new IllegalStateException("ppv.security.google-signin.client-id must not be blank when enabled");
        }
        if (clientSecret == null || clientSecret.trim().isEmpty()) {
            throw new IllegalStateException("ppv.security.google-signin.client-secret must not be blank when enabled");
        }
        if (txnKeyFile == null || txnKeyFile.trim().isEmpty()) {
            throw new IllegalStateException("ppv.security.google-signin.txn-key-file must not be blank when enabled");
        }
    }
}
