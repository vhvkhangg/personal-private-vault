package com.vhvkhangg.personalprivatevault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bootstrap entry point for the Personal Private Vault backend.
 *
 * <p>This root package intentionally contains only application bootstrap concerns. Business
 * capabilities live in direct sub-packages, which Spring Modulith discovers as application
 * modules.
 */
@SpringBootApplication
public class PersonalPrivateVaultApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersonalPrivateVaultApplication.class, args);
    }
}
