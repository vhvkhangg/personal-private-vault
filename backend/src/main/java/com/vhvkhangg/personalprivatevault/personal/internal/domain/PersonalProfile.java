package com.vhvkhangg.personalprivatevault.personal.internal.domain;

import com.vhvkhangg.personalprivatevault.personal.enums.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Personal profile entity mapped to {@code personal_profiles}.
 */
@Entity
@Table(name = "personal_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonalProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "relationship", length = 100, nullable = false)
    private String relationship;

    @Column(name = "is_self", nullable = false)
    private boolean isSelf;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "nationality_code", length = 2)
    private String nationalityCode;

    @Column(name = "phone", length = 64)
    private String phone;

    @Column(name = "email", length = 320)
    private String email;

    @Column(name = "address_id")
    private Long addressId;

    @Column(name = "occupation", length = 255)
    private String occupation;

    @Column(name = "notes_markdown", columnDefinition = "text")
    private String notesMarkdown;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public PersonalProfile(
            String name,
            String relationship,
            boolean isSelf,
            Gender gender,
            LocalDate birthDate,
            String nationalityCode,
            String phone,
            String email,
            Long addressId,
            String occupation,
            String notesMarkdown
    ) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.relationship = Objects.requireNonNull(relationship, "relationship must not be null");
        this.isSelf = isSelf;
        this.gender = gender;
        this.birthDate = birthDate;
        this.nationalityCode = nationalityCode;
        this.phone = phone;
        this.email = email;
        this.addressId = addressId;
        this.occupation = occupation;
        this.notesMarkdown = notesMarkdown;
    }

    @PrePersist
    protected void onPrePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onPreUpdate() {
        updatedAt = Instant.now();
    }

    public void update(
            String name,
            String relationship,
            boolean isSelf,
            Gender gender,
            LocalDate birthDate,
            String nationalityCode,
            String phone,
            String email,
            Long addressId,
            String occupation,
            String notesMarkdown
    ) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.relationship = Objects.requireNonNull(relationship, "relationship must not be null");
        this.isSelf = isSelf;
        this.gender = gender;
        this.birthDate = birthDate;
        this.nationalityCode = nationalityCode;
        this.phone = phone;
        this.email = email;
        this.addressId = addressId;
        this.occupation = occupation;
        this.notesMarkdown = notesMarkdown;
        this.updatedAt = Instant.now();
    }

    public void softDelete() {
        if (this.deletedAt == null) {
            this.deletedAt = Instant.now();
            this.updatedAt = Instant.now();
        }
    }

    public void restore() {
        if (this.deletedAt != null) {
            this.deletedAt = null;
            this.updatedAt = Instant.now();
        }
    }
}
