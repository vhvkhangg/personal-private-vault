package com.vhvkhangg.personalprivatevault.feed.conversion;

import com.vhvkhangg.personalprivatevault.feed.view.SavedResourceConversionView;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeInformationItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeNoteCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeStudyItemCommand;

import java.util.List;

/**
 * Public synchronous API for explicitly converting saved resources into Knowledge content (Study, Information, Note)
 * and querying conversion provenance.
 */
public interface SavedResourceConversionOperations {

    SavedResourceConversionView convertSavedResourceToStudy(Long savedResourceId, CreateKnowledgeStudyItemCommand command);

    SavedResourceConversionView convertSavedResourceToInformation(Long savedResourceId, CreateKnowledgeInformationItemCommand command);

    SavedResourceConversionView convertSavedResourceToNote(Long savedResourceId, CreateKnowledgeNoteCommand command);

    List<SavedResourceConversionView> findConversionsBySavedResourceId(Long savedResourceId, int limit);
}
