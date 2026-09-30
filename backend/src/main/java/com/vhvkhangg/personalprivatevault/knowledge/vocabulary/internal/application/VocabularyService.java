package com.vhvkhangg.personalprivatevault.knowledge.vocabulary.internal.application;

import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.enums.SrsReviewResponse;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.enums.VocabularyLearningStatus;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.internal.domain.VocabularyItem;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.internal.domain.VocabularyReview;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.internal.infrastructure.persistence.VocabularyItemRepository;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.internal.infrastructure.persistence.VocabularyReviewRepository;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.view.VocabularyItemView;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.view.VocabularyReviewTransitionResultView;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.view.VocabularyReviewView;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.vocabulary.CreateVocabularyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.vocabulary.InvalidVocabularyItemException;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.vocabulary.UpdateVocabularyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.vocabulary.VocabularyItemNotFoundException;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.vocabulary.VocabularyOperations;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.vocabulary.VocabularyReviewTransitionCommand;
import com.vhvkhangg.personalprivatevault.reference.catalog.ReferenceCatalog;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VocabularyService implements VocabularyOperations {

    private static final BigDecimal MAX_EASE_FACTOR = new BigDecimal("999.99");

    private final VocabularyItemRepository vocabularyItemRepository;
    private final VocabularyReviewRepository vocabularyReviewRepository;
    private final VaultEntryOperations vaultEntryOperations;
    private final ReferenceCatalog referenceCatalog;
    private final EntityManager entityManager;

    private BigDecimal validateAndScaleEaseFactor(BigDecimal rawEaseFactor, String fieldName) {
        if (rawEaseFactor == null) {
            throw new InvalidVocabularyItemException(fieldName + " must not be null");
        }
        BigDecimal scaled = rawEaseFactor.setScale(2, RoundingMode.HALF_UP);
        if (scaled.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidVocabularyItemException(fieldName + " must be greater than zero: " + rawEaseFactor);
        }
        if (scaled.compareTo(MAX_EASE_FACTOR) > 0) {
            throw new InvalidVocabularyItemException(fieldName + " must not exceed 999.99: " + rawEaseFactor);
        }
        return scaled;
    }

    @Override
    @Transactional
    public VocabularyItemView create(CreateVocabularyItemCommand command) {
        if (command == null) {
            throw new InvalidVocabularyItemException("CreateVocabularyItemCommand must not be null");
        }

        ValidatedVocabularyItem validated = validateVocabularyItem(
                command.word(),
                command.languageCode(),
                command.meaning(),
                command.example(),
                command.pronunciation(),
                command.ipa(),
                command.partOfSpeech(),
                command.sourceName(),
                command.sourceUrl(),
                command.learningStatus(),
                command.nextReviewAt(),
                command.intervalDays(),
                command.easeFactor(),
                command.repetitionCount(),
                command.lapseCount()
        );

        VaultEntryView vaultEntry = vaultEntryOperations.create(VaultEntryType.VOCABULARY);

        VocabularyItem item = new VocabularyItem(
                vaultEntry.id(),
                validated.word(),
                validated.languageCode(),
                validated.meaning(),
                validated.example(),
                validated.pronunciation(),
                validated.ipa(),
                validated.partOfSpeech(),
                validated.sourceName(),
                validated.sourceUrl(),
                validated.learningStatus(),
                validated.nextReviewAt(),
                validated.intervalDays(),
                validated.easeFactor(),
                validated.repetitionCount(),
                validated.lapseCount(),
                true
        );

        vocabularyItemRepository.saveAndFlush(item);

        return toView(item);
    }

    @Override
    @Transactional
    public VocabularyItemView update(Long id, UpdateVocabularyItemCommand command) {
        if (id == null) {
            throw new InvalidVocabularyItemException("Vocabulary item ID must not be null");
        }
        if (command == null) {
            throw new InvalidVocabularyItemException("UpdateVocabularyItemCommand must not be null");
        }

        VocabularyItem item = vocabularyItemRepository.findById(id)
                .orElseThrow(() -> new VocabularyItemNotFoundException(id));

        ValidatedVocabularyItem validated = validateVocabularyItem(
                command.word(),
                command.languageCode(),
                command.meaning(),
                command.example(),
                command.pronunciation(),
                command.ipa(),
                command.partOfSpeech(),
                command.sourceName(),
                command.sourceUrl(),
                command.learningStatus(),
                command.nextReviewAt(),
                command.intervalDays(),
                command.easeFactor(),
                command.repetitionCount(),
                command.lapseCount()
        );

        item.update(
                validated.word(),
                validated.languageCode(),
                validated.meaning(),
                validated.example(),
                validated.pronunciation(),
                validated.ipa(),
                validated.partOfSpeech(),
                validated.sourceName(),
                validated.sourceUrl(),
                validated.learningStatus(),
                validated.nextReviewAt(),
                validated.intervalDays(),
                validated.easeFactor(),
                validated.repetitionCount(),
                validated.lapseCount()
        );

        vocabularyItemRepository.saveAndFlush(item);

        return toView(item);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VocabularyItemView> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return vocabularyItemRepository.findById(id).map(this::toView);
    }

    @Override
    @Transactional
    public VocabularyReviewTransitionResultView reviewTransition(Long id, VocabularyReviewTransitionCommand command) {
        if (id == null) {
            throw new InvalidVocabularyItemException("Vocabulary item ID must not be null");
        }
        if (command == null) {
            throw new InvalidVocabularyItemException("VocabularyReviewTransitionCommand must not be null");
        }
        if (command.response() == null) {
            throw new InvalidVocabularyItemException("SRS review response must not be null");
        }
        if (command.learningStatus() == null) {
            throw new InvalidVocabularyItemException("Learning status must not be null");
        }
        if (command.newIntervalDays() == null || command.newIntervalDays() < 0) {
            throw new InvalidVocabularyItemException("New interval days must be nonnegative: " + command.newIntervalDays());
        }
        BigDecimal scaledNewEaseFactor = validateAndScaleEaseFactor(command.newEaseFactor(), "New ease factor");
        if (command.repetitionCount() == null || command.repetitionCount() < 0) {
            throw new InvalidVocabularyItemException("Repetition count must be nonnegative: " + command.repetitionCount());
        }
        if (command.lapseCount() == null || command.lapseCount() < 0) {
            throw new InvalidVocabularyItemException("Lapse count must be nonnegative: " + command.lapseCount());
        }

        VocabularyItem item = vocabularyItemRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new VocabularyItemNotFoundException(id));
        entityManager.refresh(item);

        Integer previousIntervalDays = item.getIntervalDays();
        BigDecimal previousEaseFactor = item.getEaseFactor();

        Instant reviewedAt = command.reviewedAt() != null ? command.reviewedAt() : Instant.now();

        VocabularyReview review = new VocabularyReview(
                item.getId(),
                command.response(),
                reviewedAt,
                previousIntervalDays,
                command.newIntervalDays(),
                previousEaseFactor,
                scaledNewEaseFactor,
                command.nextReviewAt()
        );

        vocabularyReviewRepository.save(review);

        item.applyReviewTransition(
                command.learningStatus(),
                command.nextReviewAt(),
                command.newIntervalDays(),
                scaledNewEaseFactor,
                command.repetitionCount(),
                command.lapseCount()
        );

        vocabularyItemRepository.saveAndFlush(item);

        return new VocabularyReviewTransitionResultView(toView(item), toView(review));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VocabularyItemView> findDue(Instant cutoff, int limit) {
        if (cutoff == null) {
            throw new InvalidVocabularyItemException("Cutoff must not be null");
        }
        if (limit <= 0) {
            throw new InvalidVocabularyItemException("Limit must be positive: " + limit);
        }

        PageRequest pageRequest = PageRequest.of(0, limit);
        return vocabularyItemRepository.findDueItems(
                VocabularyLearningStatus.MASTERED,
                VocabularyLearningStatus.NEW,
                cutoff,
                pageRequest
        )
                .stream()
                .map(this::toView)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VocabularyReviewView> findReviewsByVocabularyId(Long vocabularyId, int limit) {
        if (vocabularyId == null) {
            throw new InvalidVocabularyItemException("Vocabulary ID must not be null");
        }
        if (limit <= 0) {
            throw new InvalidVocabularyItemException("Limit must be positive: " + limit);
        }

        PageRequest pageRequest = PageRequest.of(0, limit);
        return vocabularyReviewRepository.findByVocabularyIdOrderByReviewedAtDesc(vocabularyId, pageRequest)
                .stream()
                .map(this::toView)
                .toList();
    }

    private ValidatedVocabularyItem validateVocabularyItem(
            String rawWord,
            String rawLanguageCode,
            String rawMeaning,
            String rawExample,
            String rawPronunciation,
            String rawIpa,
            String rawPartOfSpeech,
            String rawSourceName,
            String rawSourceUrl,
            VocabularyLearningStatus rawLearningStatus,
            Instant nextReviewAt,
            Integer rawIntervalDays,
            BigDecimal rawEaseFactor,
            Integer rawRepetitionCount,
            Integer rawLapseCount
    ) {
        if (rawWord == null || rawWord.isBlank()) {
            throw new InvalidVocabularyItemException("Vocabulary word must not be blank");
        }
        String word = rawWord.trim();
        if (word.length() > 500) {
            throw new InvalidVocabularyItemException("Vocabulary word must not exceed 500 characters");
        }

        if (rawLanguageCode == null || rawLanguageCode.isBlank()) {
            throw new InvalidVocabularyItemException("Language code must not be blank");
        }
        String languageCode = rawLanguageCode.trim().toLowerCase(Locale.ROOT);
        if (languageCode.length() > 10) {
            throw new InvalidVocabularyItemException("Language code must not exceed 10 characters");
        }
        if (referenceCatalog.language(languageCode).isEmpty()) {
            throw new InvalidVocabularyItemException("Language code '" + languageCode + "' does not exist in reference catalog");
        }

        if (rawMeaning == null || rawMeaning.isBlank()) {
            throw new InvalidVocabularyItemException("Vocabulary meaning must not be blank");
        }
        String meaning = rawMeaning.trim();

        String pronunciation = trimOrNull(rawPronunciation);
        if (pronunciation != null && pronunciation.length() > 500) {
            throw new InvalidVocabularyItemException("Pronunciation must not exceed 500 characters");
        }

        String ipa = trimOrNull(rawIpa);
        if (ipa != null && ipa.length() > 255) {
            throw new InvalidVocabularyItemException("IPA must not exceed 255 characters");
        }

        String partOfSpeech = trimOrNull(rawPartOfSpeech);
        if (partOfSpeech != null && partOfSpeech.length() > 100) {
            throw new InvalidVocabularyItemException("Part of speech must not exceed 100 characters");
        }

        String sourceName = trimOrNull(rawSourceName);
        if (sourceName != null && sourceName.length() > 500) {
            throw new InvalidVocabularyItemException("Source name must not exceed 500 characters");
        }

        String sourceUrl = trimOrNull(rawSourceUrl);
        if (sourceUrl != null && sourceUrl.length() > 2048) {
            throw new InvalidVocabularyItemException("Source URL must not exceed 2048 characters");
        }

        VocabularyLearningStatus learningStatus = rawLearningStatus != null ? rawLearningStatus : VocabularyLearningStatus.NEW;

        int intervalDays = rawIntervalDays != null ? rawIntervalDays : 0;
        if (intervalDays < 0) {
            throw new InvalidVocabularyItemException("Interval days must be nonnegative: " + intervalDays);
        }

        BigDecimal rawOrDefaultEase = rawEaseFactor != null ? rawEaseFactor : new BigDecimal("2.50");
        BigDecimal easeFactor = validateAndScaleEaseFactor(rawOrDefaultEase, "Ease factor");

        int repetitionCount = rawRepetitionCount != null ? rawRepetitionCount : 0;
        if (repetitionCount < 0) {
            throw new InvalidVocabularyItemException("Repetition count must be nonnegative: " + repetitionCount);
        }

        int lapseCount = rawLapseCount != null ? rawLapseCount : 0;
        if (lapseCount < 0) {
            throw new InvalidVocabularyItemException("Lapse count must be nonnegative: " + lapseCount);
        }

        return new ValidatedVocabularyItem(
                word,
                languageCode,
                meaning,
                trimOrNull(rawExample),
                pronunciation,
                ipa,
                partOfSpeech,
                sourceName,
                sourceUrl,
                learningStatus,
                nextReviewAt,
                intervalDays,
                easeFactor,
                repetitionCount,
                lapseCount
        );
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private VocabularyItemView toView(VocabularyItem item) {
        return new VocabularyItemView(
                item.getId(),
                item.getWord(),
                item.getLanguageCode(),
                item.getMeaning(),
                item.getExample(),
                item.getPronunciation(),
                item.getIpa(),
                item.getPartOfSpeech(),
                item.getSourceName(),
                item.getSourceUrl(),
                item.getLearningStatus(),
                item.getNextReviewAt(),
                item.getIntervalDays(),
                item.getEaseFactor(),
                item.getRepetitionCount(),
                item.getLapseCount()
        );
    }

    private VocabularyReviewView toView(VocabularyReview review) {
        return new VocabularyReviewView(
                review.getId(),
                review.getVocabularyId(),
                review.getResponse(),
                review.getReviewedAt(),
                review.getPreviousIntervalDays(),
                review.getNewIntervalDays(),
                review.getPreviousEaseFactor(),
                review.getNewEaseFactor(),
                review.getNextReviewAt()
        );
    }

    private record ValidatedVocabularyItem(
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
    ) {}
}
