package com.vhvkhangg.personalprivatevault.knowledge.information.internal.application;

import com.vhvkhangg.personalprivatevault.knowledge.information.enums.InformationType;
import com.vhvkhangg.personalprivatevault.knowledge.information.information.CreateInformationItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.information.information.InformationItemNotFoundException;
import com.vhvkhangg.personalprivatevault.knowledge.information.information.InformationItemOperations;
import com.vhvkhangg.personalprivatevault.knowledge.information.information.InvalidInformationItemException;
import com.vhvkhangg.personalprivatevault.knowledge.information.information.UpdateInformationItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.information.internal.domain.InformationItem;
import com.vhvkhangg.personalprivatevault.knowledge.information.internal.infrastructure.persistence.InformationItemRepository;
import com.vhvkhangg.personalprivatevault.knowledge.information.view.InformationItemView;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InformationItemService implements InformationItemOperations {

    private final InformationItemRepository informationItemRepository;
    private final VaultEntryOperations vaultEntryOperations;

    @Override
    @Transactional
    public InformationItemView create(CreateInformationItemCommand command) {
        if (command == null) {
            throw new InvalidInformationItemException("CreateInformationItemCommand must not be null");
        }

        ValidatedInformationItem validated = validateInformationItem(
                command.title(),
                command.type(),
                command.description(),
                command.contentMarkdown(),
                command.example(),
                command.sourceName(),
                command.sourceUrl()
        );

        VaultEntryView vaultEntry = vaultEntryOperations.create(VaultEntryType.INFORMATION);

        InformationItem item = new InformationItem(
                vaultEntry.id(),
                validated.title(),
                validated.type(),
                validated.description(),
                validated.contentMarkdown(),
                validated.example(),
                validated.sourceName(),
                validated.sourceUrl(),
                true
        );

        informationItemRepository.saveAndFlush(item);

        return toView(item);
    }

    @Override
    @Transactional
    public InformationItemView update(Long id, UpdateInformationItemCommand command) {
        if (id == null) {
            throw new InvalidInformationItemException("Information item ID must not be null");
        }
        if (command == null) {
            throw new InvalidInformationItemException("UpdateInformationItemCommand must not be null");
        }

        InformationItem item = informationItemRepository.findById(id)
                .orElseThrow(() -> new InformationItemNotFoundException(id));

        ValidatedInformationItem validated = validateInformationItem(
                command.title(),
                command.type(),
                command.description(),
                command.contentMarkdown(),
                command.example(),
                command.sourceName(),
                command.sourceUrl()
        );

        item.update(
                validated.title(),
                validated.type(),
                validated.description(),
                validated.contentMarkdown(),
                validated.example(),
                validated.sourceName(),
                validated.sourceUrl()
        );

        informationItemRepository.saveAndFlush(item);

        return toView(item);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InformationItemView> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return informationItemRepository.findById(id).map(this::toView);
    }

    private ValidatedInformationItem validateInformationItem(
            String rawTitle,
            InformationType type,
            String rawDescription,
            String rawContentMarkdown,
            String rawExample,
            String rawSourceName,
            String rawSourceUrl
    ) {
        if (rawTitle == null || rawTitle.isBlank()) {
            throw new InvalidInformationItemException("Information title must not be blank");
        }
        String title = rawTitle.trim();
        if (title.length() > 500) {
            throw new InvalidInformationItemException("Information title must not exceed 500 characters");
        }

        if (type == null) {
            throw new InvalidInformationItemException("Information type must not be null");
        }

        String sourceName = trimOrNull(rawSourceName);
        if (sourceName != null && sourceName.length() > 500) {
            throw new InvalidInformationItemException("Source name must not exceed 500 characters");
        }

        String sourceUrl = trimOrNull(rawSourceUrl);
        if (sourceUrl != null && sourceUrl.length() > 2048) {
            throw new InvalidInformationItemException("Source URL must not exceed 2048 characters");
        }

        return new ValidatedInformationItem(
                title,
                type,
                trimOrNull(rawDescription),
                trimOrNull(rawContentMarkdown),
                trimOrNull(rawExample),
                sourceName,
                sourceUrl
        );
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private InformationItemView toView(InformationItem item) {
        return new InformationItemView(
                item.getId(),
                item.getTitle(),
                item.getType(),
                item.getDescription(),
                item.getContentMarkdown(),
                item.getExample(),
                item.getSourceName(),
                item.getSourceUrl()
        );
    }

    private record ValidatedInformationItem(
            String title,
            InformationType type,
            String description,
            String contentMarkdown,
            String example,
            String sourceName,
            String sourceUrl
    ) {}
}
