package com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security;

import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.oauth.GoogleSignInProviderClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GoogleSignInProperties.class)
@ConditionalOnProperty(name = "ppv.security.google-signin.enabled", havingValue = "true")
public class GoogleSignInConfiguration {

    @Bean
    public GoogleSignInResources googleSignInResources(Clock clock) {
        return new GoogleSignInResources(clock);
    }

    @Bean
    public GoogleSignInTransactionCrypto googleSignInTransactionCrypto(GoogleSignInProperties properties) {
        properties.validate();
        return new GoogleSignInTransactionCrypto(properties);
    }

    @Bean
    @ConditionalOnMissingBean(GoogleSignInProviderClient.class)
    public GoogleSignInProviderClient googleSignInProviderClient(
            GoogleSignInProperties properties,
            Clock clock,
            GoogleSignInResources resources
    ) {
        properties.validate();
        return new GoogleSignInProviderClient(
                properties,
                clock,
                resources
        );
    }
}
