package com.vhvkhangg.personalprivatevault.knowledge.vocabulary.internal.domain;

import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.enums.SrsReviewResponse;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Vocabulary review history entity stored in {@code vocabulary_reviews}.
 */
@Entity
@Table(name = "vocabulary_reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VocabularyReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "vocabulary_id", nullable = false)
    private Long vocabularyId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "response", nullable = false)
    private SrsReviewResponse response;

    @Column(name = "reviewed_at", nullable = false)
    private Instant reviewedAt;

    @Column(name = "previous_interval_days")
    private Integer previousIntervalDays;

    @Column(name = "new_interval_days")
    private Integer newIntervalDays;

    @Column(name = "previous_ease_factor", precision = 5, scale = 2)
    private BigDecimal previousEaseFactor;

    @Column(name = "new_ease_factor", precision = 5, scale = 2)
    private BigDecimal newEaseFactor;

    @Column(name = "next_review_at")
    private Instant nextReviewAt;

    public VocabularyReview(
            Long vocabularyId,
            SrsReviewResponse response,
            Instant reviewedAt,
            Integer previousIntervalDays,
            Integer newIntervalDays,
            BigDecimal previousEaseFactor,
            BigDecimal newEaseFactor,
            Instant nextReviewAt
    ) {
        this.vocabularyId = Objects.requireNonNull(vocabularyId, "vocabularyId must not be null");
        this.response = Objects.requireNonNull(response, "response must not be null");
        this.reviewedAt = Objects.requireNonNull(reviewedAt, "reviewedAt must not be null");
        this.previousIntervalDays = previousIntervalDays;
        this.newIntervalDays = newIntervalDays;
        this.previousEaseFactor = previousEaseFactor;
        this.newEaseFactor = newEaseFactor;
        this.nextReviewAt = nextReviewAt;
    }
}
