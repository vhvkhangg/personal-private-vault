package com.vhvkhangg.personalprivatevault.fiction.internal.application;

import com.vhvkhangg.personalprivatevault.fiction.fiction.exception.FictionNotFoundException;
import com.vhvkhangg.personalprivatevault.fiction.internal.domain.FictionLink;
import com.vhvkhangg.personalprivatevault.fiction.internal.infrastructure.persistence.FictionLinkRepository;
import com.vhvkhangg.personalprivatevault.fiction.internal.infrastructure.persistence.FictionRepository;
import com.vhvkhangg.personalprivatevault.fiction.link.FictionLinkOperations;
import com.vhvkhangg.personalprivatevault.fiction.link.command.CreateFictionLinkCommand;
import com.vhvkhangg.personalprivatevault.fiction.link.command.UpdateFictionLinkCommand;
import com.vhvkhangg.personalprivatevault.fiction.link.exception.FictionLinkNotFoundException;
import com.vhvkhangg.personalprivatevault.fiction.link.exception.InvalidFictionLinkException;
import com.vhvkhangg.personalprivatevault.fiction.view.FictionLinkView;
import com.vhvkhangg.personalprivatevault.reference.catalog.ReferenceCatalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Application service implementing {@link FictionLinkOperations}.
 */
@Service
public class FictionLinkService implements FictionLinkOperations {

    private final FictionLinkRepository fictionLinkRepository;
    private final FictionRepository fictionRepository;
    private final ReferenceCatalog referenceCatalog;
    private final Clock clock;

    @Autowired
    public FictionLinkService(
            FictionLinkRepository fictionLinkRepository,
            FictionRepository fictionRepository,
            ReferenceCatalog referenceCatalog,
            @Autowired(required = false) Clock clock) {
        this.fictionLinkRepository = Objects.requireNonNull(fictionLinkRepository, "fictionLinkRepository must not be null");
        this.fictionRepository = Objects.requireNonNull(fictionRepository, "fictionRepository must not be null");
        this.referenceCatalog = Objects.requireNonNull(referenceCatalog, "referenceCatalog must not be null");
        this.clock = clock != null ? clock : Clock.systemUTC();
    }

    public FictionLinkService(
            FictionLinkRepository fictionLinkRepository,
            FictionRepository fictionRepository,
            ReferenceCatalog referenceCatalog) {
        this(fictionLinkRepository, fictionRepository, referenceCatalog, Clock.systemUTC());
    }

    @Override
    @Transactional
    public FictionLinkView create(CreateFictionLinkCommand command) {
        if (command == null) {
            throw new InvalidFictionLinkException("CreateFictionLinkCommand must not be null");
        }
        validateFictionExists(command.fictionId());
        String linkType = validateLinkType(command.linkType());
        String url = validateUrl(command.url());
        String label = validateLabel(command.label());
        String languageCode = validateLanguageCode(command.languageCode());
        boolean isPrimary = command.isPrimary() != null && command.isPrimary();

        FictionLink link = new FictionLink(
                command.fictionId(),
                languageCode,
                linkType,
                label,
                url,
                isPrimary,
                clock.instant()
        );
        FictionLink saved = fictionLinkRepository.save(link);
        return toView(saved);
    }

    @Override
    @Transactional
    public FictionLinkView update(UpdateFictionLinkCommand command) {
        if (command == null) {
            throw new InvalidFictionLinkException("UpdateFictionLinkCommand must not be null");
        }
        if (command.id() == null) {
            throw new InvalidFictionLinkException("Fiction link id must not be null");
        }
        if (command.fictionId() == null) {
            throw new InvalidFictionLinkException("Fiction id must not be null");
        }
        validateFictionExists(command.fictionId());

        FictionLink link = fictionLinkRepository.findById(command.id())
                .orElseThrow(() -> new FictionLinkNotFoundException(command.id()));

        if (!Objects.equals(link.getFictionId(), command.fictionId())) {
            throw new FictionLinkNotFoundException("Fiction link with ID " + command.id() + " does not belong to fiction " + command.fictionId());
        }

        String linkType = validateLinkType(command.linkType());
        String url = validateUrl(command.url());
        String label = validateLabel(command.label());
        String languageCode = validateLanguageCode(command.languageCode());
        boolean isPrimary = command.isPrimary() != null && command.isPrimary();

        link.update(languageCode, linkType, label, url, isPrimary);
        FictionLink saved = fictionLinkRepository.save(link);
        return toView(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FictionLinkView> findByFictionId(Long fictionId) {
        if (fictionId == null) {
            throw new InvalidFictionLinkException("fictionId must not be null");
        }
        validateFictionExists(fictionId);
        return fictionLinkRepository.findByFictionIdOrderByCreatedAtAsc(fictionId).stream()
                .map(this::toView)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FictionLinkView> findById(Long fictionId, Long linkId) {
        if (fictionId == null || linkId == null) {
            return Optional.empty();
        }
        return fictionLinkRepository.findByIdAndFictionId(linkId, fictionId).map(this::toView);
    }

    private void validateFictionExists(Long fictionId) {
        if (fictionId == null) {
            throw new InvalidFictionLinkException("fictionId must not be null");
        }
        if (!fictionRepository.existsById(fictionId)) {
            throw new FictionNotFoundException(fictionId);
        }
    }

    private String validateLinkType(String linkType) {
        if (linkType == null || linkType.isBlank()) {
            throw new InvalidFictionLinkException("linkType must not be blank");
        }
        String trimmed = linkType.trim();
        if (trimmed.length() > 50) {
            throw new InvalidFictionLinkException("linkType must not exceed 50 characters");
        }
        return trimmed;
    }

    private String validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new InvalidFictionLinkException("url must not be blank");
        }
        String trimmed = url.trim();
        if (trimmed.length() > 2048) {
            throw new InvalidFictionLinkException("url must not exceed 2048 characters");
        }
        return trimmed;
    }

    private String validateLabel(String label) {
        if (label == null || label.isBlank()) {
            return null;
        }
        String trimmed = label.trim();
        if (trimmed.length() > 255) {
            throw new InvalidFictionLinkException("label must not exceed 255 characters");
        }
        return trimmed;
    }

    private String validateLanguageCode(String languageCode) {
        if (languageCode == null || languageCode.isBlank()) {
            return null;
        }
        String trimmed = languageCode.trim();
        if (trimmed.length() > 10) {
            throw new InvalidFictionLinkException("languageCode must not exceed 10 characters");
        }
        if (referenceCatalog.language(trimmed).isEmpty()) {
            throw new InvalidFictionLinkException("Language code '" + trimmed + "' does not exist in reference catalog");
        }
        return trimmed;
    }

    private FictionLinkView toView(FictionLink link) {
        return new FictionLinkView(
                link.getId(),
                link.getFictionId(),
                link.getLanguageCode(),
                link.getLinkType(),
                link.getLabel(),
                link.getUrl(),
                link.isPrimary(),
                link.getCreatedAt()
        );
    }
}
