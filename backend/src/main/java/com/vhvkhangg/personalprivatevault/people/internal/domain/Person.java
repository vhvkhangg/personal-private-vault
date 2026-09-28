package com.vhvkhangg.personalprivatevault.people.internal.domain;

import com.vhvkhangg.personalprivatevault.people.enums.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Person profile entity sharing identity with {@code vault_entries.id}.
 */
@Entity
@Table(name = "persons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Person implements Persistable<Long> {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "height_cm", precision = 6, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", precision = 6, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "nationality_code", length = 2)
    private String nationalityCode;

    @Column(name = "notes")
    private String notes;

    @Transient
    private boolean isNew = false;

    public Person(
            Long id,
            String name,
            String avatarUrl,
            Gender gender,
            LocalDate birthDate,
            BigDecimal heightCm,
            BigDecimal weightKg,
            String nationalityCode,
            String notes) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.avatarUrl = avatarUrl;
        this.gender = gender;
        this.birthDate = birthDate;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.nationalityCode = nationalityCode;
        this.notes = notes;
        this.isNew = true;
    }

    public void update(
            String name,
            String avatarUrl,
            Gender gender,
            LocalDate birthDate,
            BigDecimal heightCm,
            BigDecimal weightKg,
            String nationalityCode,
            String notes) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.avatarUrl = avatarUrl;
        this.gender = gender;
        this.birthDate = birthDate;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.nationalityCode = nationalityCode;
        this.notes = notes;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }
}
