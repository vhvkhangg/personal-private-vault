package com.vhvkhangg.personalprivatevault.location.internal.domain;

import com.vhvkhangg.personalprivatevault.location.enums.DiningServiceStyle;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class LocationDiningServiceStyleId implements Serializable {

    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "service_style", nullable = false)
    private DiningServiceStyle serviceStyle;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocationDiningServiceStyleId that)) return false;
        return Objects.equals(locationId, that.locationId) && serviceStyle == that.serviceStyle;
    }

    @Override
    public int hashCode() {
        return Objects.hash(locationId, serviceStyle);
    }
}
