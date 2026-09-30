package com.vhvkhangg.personalprivatevault.feed;

import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeNoteCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeNoteView;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeOperations;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Representative external consumer test fixture in the {@code feed} module
 * demonstrating that only {@code knowledge.api.*} types can be resolved and accessed
 * across the module boundary according to Spring Modulith encapsulation rules.
 */
@Component
public class FeedKnowledgeConsumerTestFixture {

    private final KnowledgeOperations knowledgeOperations;

    public FeedKnowledgeConsumerTestFixture(KnowledgeOperations knowledgeOperations) {
        this.knowledgeOperations = knowledgeOperations;
    }

    public KnowledgeNoteView convertSavedResourceToNote(String title, String content, String sourceUrl) {
        CreateKnowledgeNoteCommand command = new CreateKnowledgeNoteCommand(
                title,
                content,
                null,
                null,
                sourceUrl,
                null,
                null,
                null
        );
        return knowledgeOperations.createNote(command);
    }

    public Optional<KnowledgeNoteView> findNote(Long noteId) {
        return knowledgeOperations.findNoteById(noteId);
    }
}
