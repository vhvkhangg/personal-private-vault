package com.vhvkhangg.personalprivatevault.collection.software.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Composite primary key for {@link SoftwareItemPlatform}.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class SoftwareItemPlatformId implements Serializable {

    @Column(name = "software_id", nullable = false)
    private Long softwareId;

    @Column(name = "platform_id", nullable = false)
    private Long platformId;
}
