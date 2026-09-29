package com.vhvkhangg.personalprivatevault.fiction.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Composite primary key for {@link FictionWorldSettingAssignment}.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class FictionWorldSettingId implements Serializable {

    @Column(name = "fiction_id", nullable = false)
    private Long fictionId;

    @Column(name = "world_setting_id", nullable = false)
    private Long worldSettingId;
}
