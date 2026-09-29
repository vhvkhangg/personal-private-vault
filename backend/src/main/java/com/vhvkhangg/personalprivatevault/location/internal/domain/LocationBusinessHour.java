package com.vhvkhangg.personalprivatevault.location.internal.domain;

import com.vhvkhangg.personalprivatevault.location.enums.DayOfWeek;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalTime;
import java.util.Objects;

@Entity
@Table(name = "location_business_hours")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LocationBusinessHour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "sequence", nullable = false)
    private int sequence;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    public LocationBusinessHour(
            Long locationId,
            DayOfWeek dayOfWeek,
            int sequence,
            LocalTime openTime,
            LocalTime closeTime
    ) {
        this.locationId = Objects.requireNonNull(locationId, "locationId must not be null");
        this.dayOfWeek = Objects.requireNonNull(dayOfWeek, "dayOfWeek must not be null");
        this.sequence = sequence;
        this.openTime = Objects.requireNonNull(openTime, "openTime must not be null");
        this.closeTime = Objects.requireNonNull(closeTime, "closeTime must not be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocationBusinessHour other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
