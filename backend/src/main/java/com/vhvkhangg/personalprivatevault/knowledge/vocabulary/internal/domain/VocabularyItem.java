package com.vhvkhangg.personalprivatevault.knowledge.vocabulary.internal.domain;

import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.enums.VocabularyLearningStatus;
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
import java.time.Instant;
import java.util.Objects;

/**
 * Vocabulary item entity sharing identity with {@code vault_entries.id} for type {@code VOCABULARY}.
 */
@Entity
@Table(name = "vocabulary_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VocabularyItem implements Persistable<Long> {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "word", length = 500, nullable = false)
    private String word;

    @Column(name = "language_code", length = 10, nullable = false)
    private String languageCode;

    @Column(name = "meaning", columnDefinition = "text", nullable = false)
    private String meaning;

    @Column(name = "example", columnDefinition = "text")
    private String example;

    @Column(name = "pronunciation", length = 500)
    private String pronunciation;

    @Column(name = "ipa", length = 255)
    private String ipa;

    @Column(name = "part_of_speech", length = 100)
    private String partOfSpeech;

    @Column(name = "source_name", length = 500)
    private String sourceName;

    @Column(name = "source_url", length = 2048)
    private String sourceUrl;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "learning_status", nullable = false)
    private VocabularyLearningStatus learningStatus;

    @Column(name = "next_review_at")
    private Instant nextReviewAt;

    @Column(name = "interval_days", nullable = false)
    private int intervalDays;

    @Column(name = "ease_factor", precision = 5, scale = 2, nullable = false)
    private BigDecimal easeFactor;

    @Column(name = "repetition_count", nullable = false)
    private int repetitionCount;

    @Column(name = "lapse_count", nullable = false)
    private int lapseCount;

    @Transient
    private boolean isNew = false;

    public VocabularyItem(
            Long id,
            String word,
            String languageCode,
            String meaning,
            String example,
            String pronunciation,
            String ipa,
            String partOfSpeech,
            String sourceName,
            String sourceUrl,
            VocabularyLearningStatus learningStatus,
            Instant nextReviewAt,
            int intervalDays,
            BigDecimal easeFactor,
            int repetitionCount,
            int lapseCount,
            boolean isNew
    ) {
        this.id = Objects.requireNonNull(id, "VocabularyItem id must not be null");
        this.word = Objects.requireNonNull(word, "VocabularyItem word must not be null");
        this.languageCode = Objects.requireNonNull(languageCode, "VocabularyItem languageCode must not be null");
        this.meaning = Objects.requireNonNull(meaning, "VocabularyItem meaning must not be null");
        this.example = example;
        this.pronunciation = pronunciation;
        this.ipa = ipa;
        this.partOfSpeech = partOfSpeech;
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
        this.learningStatus = Objects.requireNonNull(learningStatus, "VocabularyItem learningStatus must not be null");
        this.nextReviewAt = nextReviewAt;
        this.intervalDays = intervalDays;
        this.easeFactor = Objects.requireNonNull(easeFactor, "VocabularyItem easeFactor must not be null");
        this.repetitionCount = repetitionCount;
        this.lapseCount = lapseCount;
        this.isNew = isNew;
    }

    public void update(
            String word,
            String languageCode,
            String meaning,
            String example,
            String pronunciation,
            String ipa,
            String partOfSpeech,
            String sourceName,
            String sourceUrl,
            VocabularyLearningStatus learningStatus,
            Instant nextReviewAt,
            int intervalDays,
            BigDecimal easeFactor,
            int repetitionCount,
            int lapseCount
    ) {
        this.word = Objects.requireNonNull(word, "VocabularyItem word must not be null");
        this.languageCode = Objects.requireNonNull(languageCode, "VocabularyItem languageCode must not be null");
        this.meaning = Objects.requireNonNull(meaning, "VocabularyItem meaning must not be null");
        this.example = example;
        this.pronunciation = pronunciation;
        this.ipa = ipa;
        this.partOfSpeech = partOfSpeech;
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
        this.learningStatus = Objects.requireNonNull(learningStatus, "VocabularyItem learningStatus must not be null");
        this.nextReviewAt = nextReviewAt;
        this.intervalDays = intervalDays;
        this.easeFactor = Objects.requireNonNull(easeFactor, "VocabularyItem easeFactor must not be null");
        this.repetitionCount = repetitionCount;
        this.lapseCount = lapseCount;
    }

    public void applyReviewTransition(
            VocabularyLearningStatus learningStatus,
            Instant nextReviewAt,
            int intervalDays,
            BigDecimal easeFactor,
            int repetitionCount,
            int lapseCount
    ) {
        this.learningStatus = Objects.requireNonNull(learningStatus, "learningStatus must not be null");
        this.nextReviewAt = nextReviewAt;
        this.intervalDays = intervalDays;
        this.easeFactor = Objects.requireNonNull(easeFactor, "easeFactor must not be null");
        this.repetitionCount = repetitionCount;
        this.lapseCount = lapseCount;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}
