package com.vhvkhangg.personalprivatevault.fiction.internal.application;

import com.vhvkhangg.personalprivatevault.fiction.enums.ConsumptionStatus;
import com.vhvkhangg.personalprivatevault.fiction.enums.FictionFormat;
import com.vhvkhangg.personalprivatevault.fiction.enums.ProgressStatus;
import com.vhvkhangg.personalprivatevault.fiction.fiction.FictionOperations;
import com.vhvkhangg.personalprivatevault.fiction.fiction.command.CreateFictionCommand;
import com.vhvkhangg.personalprivatevault.fiction.fiction.command.UpdateFictionCommand;
import com.vhvkhangg.personalprivatevault.fiction.fiction.exception.FictionNotFoundException;
import com.vhvkhangg.personalprivatevault.fiction.fiction.exception.InvalidFictionException;
import com.vhvkhangg.personalprivatevault.fiction.internal.domain.Fiction;
import com.vhvkhangg.personalprivatevault.fiction.internal.infrastructure.persistence.FictionGenreRepository;
import com.vhvkhangg.personalprivatevault.fiction.internal.infrastructure.persistence.FictionRepository;
import com.vhvkhangg.personalprivatevault.fiction.internal.infrastructure.persistence.FictionStoryArchetypeRepository;
import com.vhvkhangg.personalprivatevault.fiction.internal.infrastructure.persistence.FictionWorldSettingRepository;
import com.vhvkhangg.personalprivatevault.fiction.view.FictionClassificationsView;
import com.vhvkhangg.personalprivatevault.fiction.view.FictionView;
import com.vhvkhangg.personalprivatevault.people.group.CreatorGroupOperations;
import com.vhvkhangg.personalprivatevault.people.person.PersonOperations;
import com.vhvkhangg.personalprivatevault.reference.catalog.ReferenceCatalog;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Application service implementing {@link FictionOperations}.
 */
@Service
public class FictionService implements FictionOperations {

    private final FictionRepository fictionRepository;
    private final FictionGenreRepository fictionGenreRepository;
    private final FictionStoryArchetypeRepository storyArchetypeRepository;
    private final FictionWorldSettingRepository worldSettingRepository;
    private final VaultEntryOperations vaultEntryOperations;
    private final PersonOperations personOperations;
    private final CreatorGroupOperations creatorGroupOperations;
    private final ReferenceCatalog referenceCatalog;

    @Autowired
    public FictionService(
            FictionRepository fictionRepository,
            FictionGenreRepository fictionGenreRepository,
            FictionStoryArchetypeRepository storyArchetypeRepository,
            FictionWorldSettingRepository worldSettingRepository,
            VaultEntryOperations vaultEntryOperations,
            PersonOperations personOperations,
            CreatorGroupOperations creatorGroupOperations,
            ReferenceCatalog referenceCatalog) {
        this.fictionRepository = Objects.requireNonNull(fictionRepository, "fictionRepository must not be null");
        this.fictionGenreRepository = Objects.requireNonNull(fictionGenreRepository, "fictionGenreRepository must not be null");
        this.storyArchetypeRepository = Objects.requireNonNull(storyArchetypeRepository, "storyArchetypeRepository must not be null");
        this.worldSettingRepository = Objects.requireNonNull(worldSettingRepository, "worldSettingRepository must not be null");
        this.vaultEntryOperations = Objects.requireNonNull(vaultEntryOperations, "vaultEntryOperations must not be null");
        this.personOperations = Objects.requireNonNull(personOperations, "personOperations must not be null");
        this.creatorGroupOperations = Objects.requireNonNull(creatorGroupOperations, "creatorGroupOperations must not be null");
        this.referenceCatalog = Objects.requireNonNull(referenceCatalog, "referenceCatalog must not be null");
    }

    @Override
    @Transactional
    public FictionView create(CreateFictionCommand command) {
        if (command == null) {
            throw new InvalidFictionException("CreateFictionCommand must not be null");
        }
        ValidatedFiction validated = validateFiction(
                command.title(),
                command.originalTitle(),
                command.nationalityCode(),
                command.posterUrl(),
                command.format(),
                command.isNsfw(),
                command.genreId(),
                command.authorPersonId(),
                command.authorGroupId(),
                command.description(),
                command.totalChapters(),
                command.progressStatus(),
                command.consumptionStatus(),
                command.currentProgressText(),
                command.review()
        );

        if (command.storyArchetypeIds() != null) {
            for (Long archetypeId : command.storyArchetypeIds()) {
                validateStoryArchetypeExists(archetypeId);
            }
        }
        if (command.worldSettingIds() != null) {
            for (Long settingId : command.worldSettingIds()) {
                validateWorldSettingExists(settingId);
            }
        }

        VaultEntryView vaultEntry = vaultEntryOperations.create(VaultEntryType.FICTION);
        Fiction fiction = new Fiction(
                vaultEntry.id(),
                validated.title(),
                validated.originalTitle(),
                validated.nationalityCode(),
                validated.posterUrl(),
                validated.format(),
                validated.isNsfw(),
                validated.genreId(),
                validated.authorPersonId(),
                validated.authorGroupId(),
                validated.description(),
                validated.totalChapters(),
                validated.progressStatus(),
                validated.consumptionStatus(),
                validated.currentProgressText(),
                validated.review()
        );
        Fiction saved = fictionRepository.save(fiction);

        if (command.storyArchetypeIds() != null) {
            for (Long archetypeId : command.storyArchetypeIds()) {
                storyArchetypeRepository.insertIfAbsent(saved.getId(), archetypeId);
            }
        }
        if (command.worldSettingIds() != null) {
            for (Long settingId : command.worldSettingIds()) {
                worldSettingRepository.insertIfAbsent(saved.getId(), settingId);
            }
        }

        return toView(saved, loadStoryArchetypes(saved.getId()), loadWorldSettings(saved.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FictionView> find(Long fictionId) {
        if (fictionId == null) {
            return Optional.empty();
        }
        return fictionRepository.findById(fictionId).map(fiction ->
                toView(fiction, loadStoryArchetypes(fictionId), loadWorldSettings(fictionId))
        );
    }

    @Override
    @Transactional
    public FictionView update(UpdateFictionCommand command) {
        if (command == null) {
            throw new InvalidFictionException("UpdateFictionCommand must not be null");
        }
        if (command.id() == null) {
            throw new InvalidFictionException("Fiction id must not be null");
        }

        Fiction fiction = fictionRepository.findById(command.id())
                .orElseThrow(() -> new FictionNotFoundException(command.id()));

        ValidatedFiction validated = validateFiction(
                command.title(),
                command.originalTitle(),
                command.nationalityCode(),
                command.posterUrl(),
                command.format(),
                command.isNsfw(),
                command.genreId(),
                command.authorPersonId(),
                command.authorGroupId(),
                command.description(),
                command.totalChapters(),
                command.progressStatus(),
                command.consumptionStatus(),
                command.currentProgressText(),
                command.review()
        );

        fiction.update(
                validated.title(),
                validated.originalTitle(),
                validated.nationalityCode(),
                validated.posterUrl(),
                validated.format(),
                validated.isNsfw(),
                validated.genreId(),
                validated.authorPersonId(),
                validated.authorGroupId(),
                validated.description(),
                validated.totalChapters(),
                validated.progressStatus(),
                validated.consumptionStatus(),
                validated.currentProgressText(),
                validated.review()
        );
        Fiction saved = fictionRepository.save(fiction);

        return toView(saved, loadStoryArchetypes(saved.getId()), loadWorldSettings(saved.getId()));
    }

    @Override
    @Transactional
    public void addStoryArchetype(Long fictionId, Long storyArchetypeId) {
        if (fictionId == null) {
            throw new InvalidFictionException("fictionId must not be null");
        }
        if (storyArchetypeId == null) {
            throw new InvalidFictionException("storyArchetypeId must not be null");
        }
        if (!fictionRepository.existsById(fictionId)) {
            throw new FictionNotFoundException(fictionId);
        }
        validateStoryArchetypeExists(storyArchetypeId);
        storyArchetypeRepository.insertIfAbsent(fictionId, storyArchetypeId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getStoryArchetypes(Long fictionId) {
        if (fictionId == null) {
            throw new InvalidFictionException("fictionId must not be null");
        }
        if (!fictionRepository.existsById(fictionId)) {
            throw new FictionNotFoundException(fictionId);
        }
        return loadStoryArchetypes(fictionId);
    }

    @Override
    @Transactional
    public void addWorldSetting(Long fictionId, Long worldSettingId) {
        if (fictionId == null) {
            throw new InvalidFictionException("fictionId must not be null");
        }
        if (worldSettingId == null) {
            throw new InvalidFictionException("worldSettingId must not be null");
        }
        if (!fictionRepository.existsById(fictionId)) {
            throw new FictionNotFoundException(fictionId);
        }
        validateWorldSettingExists(worldSettingId);
        worldSettingRepository.insertIfAbsent(fictionId, worldSettingId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getWorldSettings(Long fictionId) {
        if (fictionId == null) {
            throw new InvalidFictionException("fictionId must not be null");
        }
        if (!fictionRepository.existsById(fictionId)) {
            throw new FictionNotFoundException(fictionId);
        }
        return loadWorldSettings(fictionId);
    }

    @Override
    @Transactional(readOnly = true)
    public FictionClassificationsView getClassifications(Long fictionId) {
        if (fictionId == null) {
            throw new InvalidFictionException("fictionId must not be null");
        }
        if (!fictionRepository.existsById(fictionId)) {
            throw new FictionNotFoundException(fictionId);
        }
        return new FictionClassificationsView(
                fictionId,
                loadStoryArchetypes(fictionId),
                loadWorldSettings(fictionId)
        );
    }

    private void validateStoryArchetypeExists(Long storyArchetypeId) {
        if (storyArchetypeId == null) {
            throw new InvalidFictionException("storyArchetypeId must not be null");
        }
        if (referenceCatalog.storyArchetype(storyArchetypeId).isEmpty()) {
            throw new InvalidFictionException("Story archetype with ID " + storyArchetypeId + " does not exist in reference catalog");
        }
    }

    private void validateWorldSettingExists(Long worldSettingId) {
        if (worldSettingId == null) {
            throw new InvalidFictionException("worldSettingId must not be null");
        }
        if (referenceCatalog.worldSetting(worldSettingId).isEmpty()) {
            throw new InvalidFictionException("World setting with ID " + worldSettingId + " does not exist in reference catalog");
        }
    }

    private Set<Long> loadStoryArchetypes(Long fictionId) {
        return Set.copyOf(storyArchetypeRepository.findStoryArchetypeIdsByFictionId(fictionId));
    }

    private Set<Long> loadWorldSettings(Long fictionId) {
        return Set.copyOf(worldSettingRepository.findWorldSettingIdsByFictionId(fictionId));
    }

    private record ValidatedFiction(
            String title,
            String originalTitle,
            String nationalityCode,
            String posterUrl,
            FictionFormat format,
            boolean isNsfw,
            Long genreId,
            Long authorPersonId,
            Long authorGroupId,
            String description,
            Integer totalChapters,
            ProgressStatus progressStatus,
            ConsumptionStatus consumptionStatus,
            String currentProgressText,
            String review
    ) {
    }

    private ValidatedFiction validateFiction(
            String rawTitle,
            String rawOriginalTitle,
            String rawNationalityCode,
            String rawPosterUrl,
            FictionFormat rawFormat,
            Boolean rawIsNsfw,
            Long rawGenreId,
            Long rawAuthorPersonId,
            Long rawAuthorGroupId,
            String rawDescription,
            Integer rawTotalChapters,
            ProgressStatus rawProgressStatus,
            ConsumptionStatus rawConsumptionStatus,
            String rawCurrentProgressText,
            String rawReview) {

        String title = validateTitle(rawTitle);
        String originalTitle = validateOriginalTitle(rawOriginalTitle);
        String nationalityCode = validateNationalityCode(rawNationalityCode);
        String posterUrl = validatePosterUrl(rawPosterUrl);

        if (rawFormat == null) {
            throw new InvalidFictionException("format must not be null");
        }
        boolean isNsfw = rawIsNsfw != null && rawIsNsfw;

        Long genreId = validateGenre(rawGenreId);
        validateAuthorSource(rawAuthorPersonId, rawAuthorGroupId);

        Integer totalChapters = validateTotalChapters(rawTotalChapters);
        ProgressStatus progressStatus = rawProgressStatus != null ? rawProgressStatus : ProgressStatus.ONGOING;
        ConsumptionStatus consumptionStatus = rawConsumptionStatus != null ? rawConsumptionStatus : ConsumptionStatus.UNCONSUMED;
        String currentProgressText = validateCurrentProgressText(rawCurrentProgressText);
        String review = validateReview(rawReview);
        String description = validateDescription(rawDescription);

        return new ValidatedFiction(
                title,
                originalTitle,
                nationalityCode,
                posterUrl,
                rawFormat,
                isNsfw,
                genreId,
                rawAuthorPersonId,
                rawAuthorGroupId,
                description,
                totalChapters,
                progressStatus,
                consumptionStatus,
                currentProgressText,
                review
        );
    }

    private String validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new InvalidFictionException("Fiction title must not be blank");
        }
        String trimmed = title.trim();
        if (trimmed.length() > 500) {
            throw new InvalidFictionException("Fiction title must not exceed 500 characters");
        }
        return trimmed;
    }

    private String validateOriginalTitle(String originalTitle) {
        if (originalTitle == null || originalTitle.isBlank()) {
            return null;
        }
        String trimmed = originalTitle.trim();
        if (trimmed.length() > 500) {
            throw new InvalidFictionException("Original title must not exceed 500 characters");
        }
        return trimmed;
    }

    private String validateNationalityCode(String nationalityCode) {
        if (nationalityCode == null || nationalityCode.isBlank()) {
            return null;
        }
        String trimmed = nationalityCode.trim();
        if (trimmed.length() != 2) {
            throw new InvalidFictionException("Nationality code must be a 2-character ISO country code: " + nationalityCode);
        }
        String normalized = trimmed.toUpperCase(Locale.ROOT);
        if (referenceCatalog.country(normalized).isEmpty()) {
            throw new InvalidFictionException("Nationality code '" + normalized + "' does not exist in reference catalog");
        }
        return normalized;
    }

    private String validatePosterUrl(String posterUrl) {
        if (posterUrl == null || posterUrl.isBlank()) {
            return null;
        }
        String trimmed = posterUrl.trim();
        if (trimmed.length() > 2048) {
            throw new InvalidFictionException("Poster URL must not exceed 2048 characters");
        }
        return trimmed;
    }

    private Long validateGenre(Long genreId) {
        if (genreId == null) {
            throw new InvalidFictionException("Fiction genreId must not be null");
        }
        if (!fictionGenreRepository.existsById(genreId)) {
            throw new InvalidFictionException("Fiction genre with ID " + genreId + " does not exist");
        }
        return genreId;
    }

    private void validateAuthorSource(Long authorPersonId, Long authorGroupId) {
        if ((authorPersonId == null && authorGroupId == null) || (authorPersonId != null && authorGroupId != null)) {
            throw new InvalidFictionException("Exactly one author source must be provided: authorPersonId XOR authorGroupId");
        }
        if (authorPersonId != null) {
            if (personOperations.find(authorPersonId).isEmpty()) {
                throw new InvalidFictionException("Author person with ID " + authorPersonId + " does not exist");
            }
        } else {
            if (creatorGroupOperations.find(authorGroupId).isEmpty()) {
                throw new InvalidFictionException("Author creator group with ID " + authorGroupId + " does not exist");
            }
        }
    }

    private Integer validateTotalChapters(Integer totalChapters) {
        if (totalChapters == null) {
            return null;
        }
        if (totalChapters < 0) {
            throw new InvalidFictionException("Total chapters must be greater than or equal to 0: " + totalChapters);
        }
        return totalChapters;
    }

    private String validateCurrentProgressText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String trimmed = text.trim();
        if (trimmed.length() > 255) {
            throw new InvalidFictionException("Current progress text must not exceed 255 characters");
        }
        return trimmed;
    }

    private String validateReview(String review) {
        if (review == null || review.isBlank()) {
            return null;
        }
        return review.trim();
    }

    private String validateDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }

    private FictionView toView(Fiction fiction, Set<Long> storyArchetypeIds, Set<Long> worldSettingIds) {
        return new FictionView(
                fiction.getId(),
                fiction.getTitle(),
                fiction.getOriginalTitle(),
                fiction.getNationalityCode(),
                fiction.getPosterUrl(),
                fiction.getFormat(),
                fiction.isNsfw(),
                fiction.getGenreId(),
                fiction.getAuthorPersonId(),
                fiction.getAuthorGroupId(),
                fiction.getDescription(),
                fiction.getTotalChapters(),
                fiction.getProgressStatus(),
                fiction.getConsumptionStatus(),
                fiction.getCurrentProgressText(),
                fiction.getReview(),
                storyArchetypeIds,
                worldSettingIds
        );
    }
}
