package com.vhvkhangg.personalprivatevault.vault.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class VaultEntryTagId implements Serializable {

    @Column(name = "vault_entry_id", nullable = false)
    private Long vaultEntryId;

    @Column(name = "tag_id", nullable = false)
    private Long tagId;
}
