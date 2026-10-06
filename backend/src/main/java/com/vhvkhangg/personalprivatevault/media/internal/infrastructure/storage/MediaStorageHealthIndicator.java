package com.vhvkhangg.personalprivatevault.media.internal.infrastructure.storage;

import com.vhvkhangg.personalprivatevault.media.internal.application.storage.MediaStoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("mediaStorage")
@RequiredArgsConstructor
public class MediaStorageHealthIndicator implements HealthIndicator {

    private final MediaStoragePort mediaStoragePort;
    private final MediaStorageProperties properties;

    @Override
    public Health health() {
        if (!properties.isEnabled()) {
            return Health.up().build();
        }

        if (mediaStoragePort.isAvailable()) {
            return Health.up().build();
        }

        return Health.down().build();
    }
}
