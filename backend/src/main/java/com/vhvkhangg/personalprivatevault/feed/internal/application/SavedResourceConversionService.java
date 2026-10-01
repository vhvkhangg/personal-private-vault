package com.vhvkhangg.personalprivatevault.feed.internal.application;

import com.vhvkhangg.personalprivatevault.feed.conversion.SavedResourceConversionOperations;
import com.vhvkhangg.personalprivatevault.feed.conversion.exception.InvalidSavedResourceConversionException;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.SavedResource;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.SavedResourceConversion;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.SavedResourceConversionRepository;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.SavedResourceRepository;
import com.vhvkhangg.personalprivatevault.feed.resource.exception.SavedResourceNotFoundException;
import com.vhvkhangg.personalprivatevault.feed.view.SavedResourceConversionView;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeInformationItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeNoteCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeStudyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeInformationItemView;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeNoteView;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeOperations;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeStudyItemView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementing explicit conversion of saved resources into Knowledge content.
 */
@Service
@RequiredArgsConstructor
public class SavedResourceConversionService implements SavedResourceConversionOperations {

    private final SavedResourceRepository savedResourceRepository;
    private final SavedResourceConversionRepository conversionRepository;
    private final KnowledgeOperations knowledgeOperations;

    @Override
    @Transactional
    public SavedResourceConversionView convertSavedResourceToStudy(Long savedResourceId, CreateKnowledgeStudyItemCommand command) {
        if (savedResourceId == null) {
            throw new InvalidSavedResourceConversionException("savedResourceId must not be null");
        }
        if (command == null) {
            throw new InvalidSavedResourceConversionException("CreateKnowledgeStudyItemCommand must not be null");
        }

        SavedResource savedResource = savedResourceRepository.findById(savedResourceId)
                .orElseThrow(() -> new SavedResourceNotFoundException(savedResourceId));

        KnowledgeStudyItemView studyItem = knowledgeOperations.createStudyItem(command);

        SavedResourceConversion conversion = new SavedResourceConversion(savedResource.getId(), studyItem.id());
        conversionRepository.save(conversion);

        return new SavedResourceConversionView(savedResource.getId(), studyItem.id(), conversion.getCreatedAt());
    }

    @Override
    @Transactional
    public SavedResourceConversionView convertSavedResourceToInformation(Long savedResourceId, CreateKnowledgeInformationItemCommand command) {
        if (savedResourceId == null) {
            throw new InvalidSavedResourceConversionException("savedResourceId must not be null");
        }
        if (command == null) {
            throw new InvalidSavedResourceConversionException("CreateKnowledgeInformationItemCommand must not be null");
        }

        SavedResource savedResource = savedResourceRepository.findById(savedResourceId)
                .orElseThrow(() -> new SavedResourceNotFoundException(savedResourceId));

        KnowledgeInformationItemView informationItem = knowledgeOperations.createInformationItem(command);

        SavedResourceConversion conversion = new SavedResourceConversion(savedResource.getId(), informationItem.id());
        conversionRepository.save(conversion);

        return new SavedResourceConversionView(savedResource.getId(), informationItem.id(), conversion.getCreatedAt());
    }

    @Override
    @Transactional
    public SavedResourceConversionView convertSavedResourceToNote(Long savedResourceId, CreateKnowledgeNoteCommand command) {
        if (savedResourceId == null) {
            throw new InvalidSavedResourceConversionException("savedResourceId must not be null");
        }
        if (command == null) {
            throw new InvalidSavedResourceConversionException("CreateKnowledgeNoteCommand must not be null");
        }

        SavedResource savedResource = savedResourceRepository.findById(savedResourceId)
                .orElseThrow(() -> new SavedResourceNotFoundException(savedResourceId));

        KnowledgeNoteView note = knowledgeOperations.createNote(command);

        SavedResourceConversion conversion = new SavedResourceConversion(savedResource.getId(), note.id());
        conversionRepository.save(conversion);

        return new SavedResourceConversionView(savedResource.getId(), note.id(), conversion.getCreatedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavedResourceConversionView> findConversionsBySavedResourceId(Long savedResourceId, int limit) {
        if (limit <= 0) {
            throw new InvalidSavedResourceConversionException("Limit must be positive");
        }
        return conversionRepository.findBySavedResourceId(savedResourceId, PageRequest.of(0, limit))
                .stream()
                .map(c -> new SavedResourceConversionView(c.getId().getSavedResourceId(), c.getId().getTargetVaultEntryId(), c.getCreatedAt()))
                .toList();
    }
}
