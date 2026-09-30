package com.vhvkhangg.personalprivatevault.knowledge.study.internal.application;

import com.vhvkhangg.personalprivatevault.account.account.ExternalAccountOperations;
import com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountType;
import com.vhvkhangg.personalprivatevault.account.view.ExternalAccountView;
import com.vhvkhangg.personalprivatevault.knowledge.study.enums.StudyStatus;
import com.vhvkhangg.personalprivatevault.knowledge.study.enums.StudyType;
import com.vhvkhangg.personalprivatevault.knowledge.study.internal.domain.StudyItem;
import com.vhvkhangg.personalprivatevault.knowledge.study.internal.infrastructure.persistence.StudyItemRepository;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.CreateStudyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.InvalidStudyItemException;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.StudyConflictException;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.StudyItemNotFoundException;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.StudyItemOperations;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.UpdateStudyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.study.view.StudyItemView;
import com.vhvkhangg.personalprivatevault.people.group.CreatorGroupOperations;
import com.vhvkhangg.personalprivatevault.people.person.PersonOperations;
import com.vhvkhangg.personalprivatevault.reference.catalog.ReferenceCatalog;
import com.vhvkhangg.personalprivatevault.reference.view.PlatformView;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class StudyItemService implements StudyItemOperations {

    private static final Pattern HOSTNAME_PATTERN = Pattern.compile(
            "^(?=.{1,255}$)[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?(?:\\.[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?)*$"
    );

    private final StudyItemRepository studyItemRepository;
    private final VaultEntryOperations vaultEntryOperations;
    private final PersonOperations personOperations;
    private final CreatorGroupOperations creatorGroupOperations;
    private final ReferenceCatalog referenceCatalog;
    private final ExternalAccountOperations externalAccountOperations;

    @Override
    @Transactional
    public StudyItemView create(CreateStudyItemCommand command) {
        if (command == null) {
            throw new InvalidStudyItemException("CreateStudyItemCommand must not be null");
        }

        ValidatedStudyItem validated = validateStudyItem(
                command.title(),
                command.type(),
                command.posterUrl(),
                command.siteDomain(),
                command.youtubeChannelAccountId(),
                command.authorPersonId(),
                command.authorGroupId(),
                command.publishedDate(),
                command.priceAmount(),
                command.currencyCode(),
                command.description(),
                command.url(),
                command.review(),
                command.learningStatus(),
                command.progressPercent(),
                command.currentProgressText(),
                null
        );

        VaultEntryView vaultEntry = vaultEntryOperations.create(VaultEntryType.STUDY);

        StudyItem item = new StudyItem(
                vaultEntry.id(),
                validated.title(),
                validated.posterUrl(),
                validated.type(),
                validated.siteDomain(),
                validated.youtubeChannelAccountId(),
                validated.authorPersonId(),
                validated.authorGroupId(),
                validated.publishedDate(),
                validated.priceAmount(),
                validated.currencyCode(),
                validated.description(),
                validated.url(),
                validated.review(),
                validated.learningStatus(),
                validated.progressPercent(),
                validated.currentProgressText(),
                true
        );

        try {
            studyItemRepository.saveAndFlush(item);
        } catch (DataIntegrityViolationException ex) {
            String constraint = extractConstraintName(ex);
            if (constraint.contains("youtube_channel_account_id")) {
                throw new StudyConflictException("A study item already exists for the YouTube channel account");
            }
            throw new StudyConflictException("Study item conflict occurred during creation");
        }

        return toView(item);
    }

    @Override
    @Transactional
    public StudyItemView update(Long id, UpdateStudyItemCommand command) {
        if (id == null) {
            throw new InvalidStudyItemException("Study item ID must not be null");
        }
        if (command == null) {
            throw new InvalidStudyItemException("UpdateStudyItemCommand must not be null");
        }

        StudyItem item = studyItemRepository.findById(id)
                .orElseThrow(() -> new StudyItemNotFoundException(id));

        ValidatedStudyItem validated = validateStudyItem(
                command.title(),
                command.type(),
                command.posterUrl(),
                command.siteDomain(),
                command.youtubeChannelAccountId(),
                command.authorPersonId(),
                command.authorGroupId(),
                command.publishedDate(),
                command.priceAmount(),
                command.currencyCode(),
                command.description(),
                command.url(),
                command.review(),
                command.learningStatus(),
                command.progressPercent(),
                command.currentProgressText(),
                id
        );

        item.update(
                validated.title(),
                validated.posterUrl(),
                validated.type(),
                validated.siteDomain(),
                validated.youtubeChannelAccountId(),
                validated.authorPersonId(),
                validated.authorGroupId(),
                validated.publishedDate(),
                validated.priceAmount(),
                validated.currencyCode(),
                validated.description(),
                validated.url(),
                validated.review(),
                validated.learningStatus(),
                validated.progressPercent(),
                validated.currentProgressText()
        );

        try {
            studyItemRepository.saveAndFlush(item);
        } catch (DataIntegrityViolationException ex) {
            String constraint = extractConstraintName(ex);
            if (constraint.contains("youtube_channel_account_id")) {
                throw new StudyConflictException("A study item already exists for the YouTube channel account");
            }
            throw new StudyConflictException("Study item conflict occurred during update");
        }

        return toView(item);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StudyItemView> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return studyItemRepository.findById(id).map(this::toView);
    }

    private ValidatedStudyItem validateStudyItem(
            String rawTitle,
            StudyType type,
            String rawPosterUrl,
            String rawSiteDomain,
            Long youtubeChannelAccountId,
            Long authorPersonId,
            Long authorGroupId,
            LocalDate publishedDate,
            BigDecimal priceAmount,
            String rawCurrencyCode,
            String rawDescription,
            String rawUrl,
            String rawReview,
            StudyStatus rawLearningStatus,
            BigDecimal progressPercent,
            String rawCurrentProgressText,
            Long currentId
    ) {
        if (rawTitle == null || rawTitle.isBlank()) {
            throw new InvalidStudyItemException("Study title must not be blank");
        }
        String title = rawTitle.trim();
        if (title.length() > 500) {
            throw new InvalidStudyItemException("Study title must not exceed 500 characters");
        }

        if (type == null) {
            throw new InvalidStudyItemException("Study type must not be null");
        }

        String posterUrl = trimOrNull(rawPosterUrl);
        if (posterUrl != null && posterUrl.length() > 2048) {
            throw new InvalidStudyItemException("Poster URL must not exceed 2048 characters");
        }

        String url = trimOrNull(rawUrl);
        if (url != null && url.length() > 2048) {
            throw new InvalidStudyItemException("URL must not exceed 2048 characters");
        }

        StudyStatus learningStatus = rawLearningStatus != null ? rawLearningStatus : StudyStatus.PLANNED;

        if (progressPercent != null) {
            if (progressPercent.compareTo(BigDecimal.ZERO) < 0 || progressPercent.compareTo(new BigDecimal("100")) > 0) {
                throw new InvalidStudyItemException("Progress percent must be between 0 and 100: " + progressPercent);
            }
        }

        String currentProgressText = trimOrNull(rawCurrentProgressText);
        if (currentProgressText != null && currentProgressText.length() > 500) {
            throw new InvalidStudyItemException("Current progress text must not exceed 500 characters");
        }

        if (priceAmount != null && priceAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidStudyItemException("Price amount must be nonnegative: " + priceAmount);
        }

        String currencyCode = trimOrNull(rawCurrencyCode);
        if (priceAmount != null && currencyCode == null) {
            throw new InvalidStudyItemException("Currency code is required when price is present");
        }
        if (currencyCode != null) {
            String normalizedCurrency = currencyCode.toUpperCase(Locale.ROOT);
            if (referenceCatalog.currency(normalizedCurrency).isEmpty()) {
                throw new InvalidStudyItemException("Currency code '" + normalizedCurrency + "' does not exist in reference catalog");
            }
            currencyCode = normalizedCurrency;
        }

        if (authorPersonId != null && authorGroupId != null) {
            throw new InvalidStudyItemException("Study item cannot have both Person and Creator Group author");
        }
        if (authorPersonId != null) {
            if (personOperations.find(authorPersonId).isEmpty()) {
                throw new InvalidStudyItemException("Author person with ID " + authorPersonId + " does not exist");
            }
        }
        if (authorGroupId != null) {
            if (creatorGroupOperations.find(authorGroupId).isEmpty()) {
                throw new InvalidStudyItemException("Author creator group with ID " + authorGroupId + " does not exist");
            }
        }

        String siteDomain = null;
        Long validatedYoutubeAccountId = null;

        if (type == StudyType.WEBSITE) {
            if (rawSiteDomain == null || rawSiteDomain.isBlank()) {
                throw new InvalidStudyItemException("Site domain is required for WEBSITE study item");
            }
            siteDomain = rawSiteDomain.trim().toLowerCase(Locale.ROOT);
            if (siteDomain.length() > 255) {
                throw new InvalidStudyItemException("Site domain must not exceed 255 characters");
            }
            if (!HOSTNAME_PATTERN.matcher(siteDomain).matches()) {
                throw new InvalidStudyItemException("Site domain must be a valid hostname without scheme, port, or path: " + rawSiteDomain);
            }
            if (url == null) {
                throw new InvalidStudyItemException("URL is required for WEBSITE study item");
            }
            if (youtubeChannelAccountId != null) {
                throw new InvalidStudyItemException("YouTube channel account must be null for WEBSITE study item");
            }
        } else if (type == StudyType.YOUTUBE_CHANNEL) {
            if (youtubeChannelAccountId == null) {
                throw new InvalidStudyItemException("YouTube channel account ID is required for YOUTUBE_CHANNEL study item");
            }
            if (rawSiteDomain != null && !rawSiteDomain.isBlank()) {
                throw new InvalidStudyItemException("Site domain must be null for YOUTUBE_CHANNEL study item");
            }
            ExternalAccountView account = externalAccountOperations.findById(youtubeChannelAccountId)
                    .orElseThrow(() -> new InvalidStudyItemException("YouTube channel account with ID " + youtubeChannelAccountId + " does not exist"));

            if (account.accountType() != ExternalAccountType.YOUTUBE_CHANNEL) {
                throw new InvalidStudyItemException("Referenced account must be of type YOUTUBE_CHANNEL, but was: " + account.accountType());
            }

            PlatformView platform = referenceCatalog.platform(account.platformId())
                    .orElseThrow(() -> new InvalidStudyItemException("Platform for account ID " + youtubeChannelAccountId + " does not exist"));

            if (platform.name() == null || !"youtube".equalsIgnoreCase(platform.name().trim())) {
                throw new InvalidStudyItemException("Referenced account must belong to the YouTube platform");
            }

            Optional<StudyItem> existingDuplicate = studyItemRepository.findByYoutubeChannelAccountId(youtubeChannelAccountId);
            if (existingDuplicate.isPresent() && (currentId == null || !existingDuplicate.get().getId().equals(currentId))) {
                throw new StudyConflictException("A study item already exists for the YouTube channel account");
            }

            validatedYoutubeAccountId = youtubeChannelAccountId;
        } else {
            if (rawSiteDomain != null && !rawSiteDomain.isBlank()) {
                throw new InvalidStudyItemException("Site domain must be null for " + type + " study item");
            }
            if (youtubeChannelAccountId != null) {
                throw new InvalidStudyItemException("YouTube channel account must be null for " + type + " study item");
            }
        }

        return new ValidatedStudyItem(
                title,
                type,
                posterUrl,
                siteDomain,
                validatedYoutubeAccountId,
                authorPersonId,
                authorGroupId,
                publishedDate,
                priceAmount,
                currencyCode,
                trimOrNull(rawDescription),
                url,
                trimOrNull(rawReview),
                learningStatus,
                progressPercent,
                currentProgressText
        );
    }

    private String extractConstraintName(DataIntegrityViolationException ex) {
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof ConstraintViolationException cve) {
                return cve.getConstraintName() != null ? cve.getConstraintName().toLowerCase(Locale.ROOT) : "";
            }
            cause = cause.getCause();
        }
        return "";
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private StudyItemView toView(StudyItem item) {
        return new StudyItemView(
                item.getId(),
                item.getTitle(),
                item.getPosterUrl(),
                item.getType(),
                item.getSiteDomain(),
                item.getYoutubeChannelAccountId(),
                item.getAuthorPersonId(),
                item.getAuthorGroupId(),
                item.getPublishedDate(),
                item.getPriceAmount(),
                item.getCurrencyCode(),
                item.getDescription(),
                item.getUrl(),
                item.getReview(),
                item.getLearningStatus(),
                item.getProgressPercent(),
                item.getCurrentProgressText()
        );
    }

    private record ValidatedStudyItem(
            String title,
            StudyType type,
            String posterUrl,
            String siteDomain,
            Long youtubeChannelAccountId,
            Long authorPersonId,
            Long authorGroupId,
            LocalDate publishedDate,
            BigDecimal priceAmount,
            String currencyCode,
            String description,
            String url,
            String review,
            StudyStatus learningStatus,
            BigDecimal progressPercent,
            String currentProgressText
    ) {}
}
