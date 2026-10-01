package com.vhvkhangg.personalprivatevault.feed.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Composite primary key for {@code saved_resource_conversions}.
 */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SavedResourceConversionId implements Serializable {

    @Column(name = "saved_resource_id", nullable = false)
    private Long savedResourceId;

    @Column(name = "target_vault_entry_id", nullable = false)
    private Long targetVaultEntryId;
}
