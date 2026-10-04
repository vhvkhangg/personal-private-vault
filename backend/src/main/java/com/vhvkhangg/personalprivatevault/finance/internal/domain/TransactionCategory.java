package com.vhvkhangg.personalprivatevault.finance.internal.domain;

import com.vhvkhangg.personalprivatevault.finance.enums.TransactionCategoryKind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Objects;

/**
 * Transaction category entity mapped to {@code transaction_categories}.
 */
@Entity
@Table(name = "transaction_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransactionCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "kind", nullable = false)
    private TransactionCategoryKind kind;

    @Column(name = "parent_category_id")
    private Long parentCategoryId;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public TransactionCategory(
            String name,
            TransactionCategoryKind kind,
            Long parentCategoryId,
            boolean active
    ) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.kind = Objects.requireNonNull(kind, "kind must not be null");
        this.parentCategoryId = parentCategoryId;
        this.active = active;
    }

    @PrePersist
    protected void onPrePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public void update(
            String name,
            TransactionCategoryKind kind,
            Long parentCategoryId,
            boolean active
    ) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.kind = Objects.requireNonNull(kind, "kind must not be null");
        this.parentCategoryId = parentCategoryId;
        this.active = active;
    }
}
