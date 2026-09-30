package com.vhvkhangg.personalprivatevault.knowledge.internal.application;

import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeInformationItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeNoteCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeStudyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeVocabularyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.InvalidKnowledgeItemException;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeConflictException;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeInformationItemView;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeInformationType;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeNoteView;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeNotFoundException;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeOperations;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeSrsReviewResponse;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeStudyItemView;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeStudyStatus;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeStudyType;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeVocabularyItemView;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeVocabularyLearningStatus;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeVocabularyReviewTransitionCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeVocabularyReviewTransitionResultView;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeVocabularyReviewView;
import com.vhvkhangg.personalprivatevault.knowledge.api.UpdateKnowledgeInformationItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.UpdateKnowledgeNoteCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.UpdateKnowledgeStudyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.UpdateKnowledgeVocabularyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.information.enums.InformationType;
import com.vhvkhangg.personalprivatevault.knowledge.information.information.CreateInformationItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.information.information.InformationItemNotFoundException;
import com.vhvkhangg.personalprivatevault.knowledge.information.information.InformationItemOperations;
import com.vhvkhangg.personalprivatevault.knowledge.information.information.InvalidInformationItemException;
import com.vhvkhangg.personalprivatevault.knowledge.information.information.UpdateInformationItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.information.view.InformationItemView;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.CreateNoteCommand;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.InvalidNoteException;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.NoteConflictException;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.NoteNotFoundException;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.NoteOperations;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.UpdateNoteCommand;
import com.vhvkhangg.personalprivatevault.knowledge.note.view.NoteView;
import com.vhvkhangg.personalprivatevault.knowledge.study.enums.StudyStatus;
import com.vhvkhangg.personalprivatevault.knowledge.study.enums.StudyType;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.CreateStudyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.InvalidStudyItemException;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.StudyConflictException;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.StudyItemNotFoundException;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.StudyItemOperations;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.UpdateStudyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.study.view.StudyItemView;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.enums.SrsReviewResponse;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.enums.VocabularyLearningStatus;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.view.VocabularyItemView;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.view.VocabularyReviewTransitionResultView;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.view.VocabularyReviewView;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.vocabulary.CreateVocabularyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.vocabulary.InvalidVocabularyItemException;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.vocabulary.UpdateVocabularyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.vocabulary.VocabularyItemNotFoundException;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.vocabulary.VocabularyOperations;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.vocabulary.VocabularyReviewTransitionCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation of {@link KnowledgeOperations} delegating to nested module contracts.
 */
@Service
@RequiredArgsConstructor
public class KnowledgeFacadeService implements KnowledgeOperations {

    private final StudyItemOperations studyItemOperations;
    private final InformationItemOperations informationItemOperations;
    private final VocabularyOperations vocabularyOperations;
    private final NoteOperations noteOperations;

    // Study
    @Override
    public KnowledgeStudyItemView createStudyItem(CreateKnowledgeStudyItemCommand command) {
        if (command == null) {
            throw new InvalidKnowledgeItemException("CreateKnowledgeStudyItemCommand must not be null");
        }
        try {
            CreateStudyItemCommand nestedCommand = new CreateStudyItemCommand(
                    command.title(),
                    command.posterUrl(),
                    mapStudyType(command.type()),
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
                    mapStudyStatus(command.learningStatus()),
                    command.progressPercent(),
                    command.currentProgressText()
            );
            return mapStudyItemView(studyItemOperations.create(nestedCommand));
        } catch (InvalidStudyItemException ex) {
            throw new InvalidKnowledgeItemException(ex.getMessage());
        } catch (StudyConflictException ex) {
            throw new KnowledgeConflictException(ex.getMessage());
        }
    }

    @Override
    public KnowledgeStudyItemView updateStudyItem(Long id, UpdateKnowledgeStudyItemCommand command) {
        if (id == null) {
            throw new InvalidKnowledgeItemException("Study item ID must not be null");
        }
        if (command == null) {
            throw new InvalidKnowledgeItemException("UpdateKnowledgeStudyItemCommand must not be null");
        }
        try {
            UpdateStudyItemCommand nestedCommand = new UpdateStudyItemCommand(
                    command.title(),
                    command.posterUrl(),
                    mapStudyType(command.type()),
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
                    mapStudyStatus(command.learningStatus()),
                    command.progressPercent(),
                    command.currentProgressText()
            );
            return mapStudyItemView(studyItemOperations.update(id, nestedCommand));
        } catch (StudyItemNotFoundException ex) {
            throw new KnowledgeNotFoundException(ex.getMessage());
        } catch (InvalidStudyItemException ex) {
            throw new InvalidKnowledgeItemException(ex.getMessage());
        } catch (StudyConflictException ex) {
            throw new KnowledgeConflictException(ex.getMessage());
        }
    }

    @Override
    public Optional<KnowledgeStudyItemView> findStudyItemById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return studyItemOperations.findById(id).map(this::mapStudyItemView);
    }

    // Information
    @Override
    public KnowledgeInformationItemView createInformationItem(CreateKnowledgeInformationItemCommand command) {
        if (command == null) {
            throw new InvalidKnowledgeItemException("CreateKnowledgeInformationItemCommand must not be null");
        }
        try {
            CreateInformationItemCommand nestedCommand = new CreateInformationItemCommand(
                    command.title(),
                    mapInformationType(command.type()),
                    command.description(),
                    command.contentMarkdown(),
                    command.example(),
                    command.sourceName(),
                    command.sourceUrl()
            );
            return mapInformationItemView(informationItemOperations.create(nestedCommand));
        } catch (InvalidInformationItemException ex) {
            throw new InvalidKnowledgeItemException(ex.getMessage());
        }
    }

    @Override
    public KnowledgeInformationItemView updateInformationItem(Long id, UpdateKnowledgeInformationItemCommand command) {
        if (id == null) {
            throw new InvalidKnowledgeItemException("Information item ID must not be null");
        }
        if (command == null) {
            throw new InvalidKnowledgeItemException("UpdateKnowledgeInformationItemCommand must not be null");
        }
        try {
            UpdateInformationItemCommand nestedCommand = new UpdateInformationItemCommand(
                    command.title(),
                    mapInformationType(command.type()),
                    command.description(),
                    command.contentMarkdown(),
                    command.example(),
                    command.sourceName(),
                    command.sourceUrl()
            );
            return mapInformationItemView(informationItemOperations.update(id, nestedCommand));
        } catch (InformationItemNotFoundException ex) {
            throw new KnowledgeNotFoundException(ex.getMessage());
        } catch (InvalidInformationItemException ex) {
            throw new InvalidKnowledgeItemException(ex.getMessage());
        }
    }

    @Override
    public Optional<KnowledgeInformationItemView> findInformationItemById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return informationItemOperations.findById(id).map(this::mapInformationItemView);
    }

    // Vocabulary
    @Override
    public KnowledgeVocabularyItemView createVocabularyItem(CreateKnowledgeVocabularyItemCommand command) {
        if (command == null) {
            throw new InvalidKnowledgeItemException("CreateKnowledgeVocabularyItemCommand must not be null");
        }
        try {
            CreateVocabularyItemCommand nestedCommand = new CreateVocabularyItemCommand(
                    command.word(),
                    command.languageCode(),
                    command.meaning(),
                    command.example(),
                    command.pronunciation(),
                    command.ipa(),
                    command.partOfSpeech(),
                    command.sourceName(),
                    command.sourceUrl(),
                    mapVocabularyLearningStatus(command.learningStatus()),
                    command.nextReviewAt(),
                    command.intervalDays(),
                    command.easeFactor(),
                    command.repetitionCount(),
                    command.lapseCount()
            );
            return mapVocabularyItemView(vocabularyOperations.create(nestedCommand));
        } catch (InvalidVocabularyItemException ex) {
            throw new InvalidKnowledgeItemException(ex.getMessage());
        }
    }

    @Override
    public KnowledgeVocabularyItemView updateVocabularyItem(Long id, UpdateKnowledgeVocabularyItemCommand command) {
        if (id == null) {
            throw new InvalidKnowledgeItemException("Vocabulary item ID must not be null");
        }
        if (command == null) {
            throw new InvalidKnowledgeItemException("UpdateKnowledgeVocabularyItemCommand must not be null");
        }
        try {
            UpdateVocabularyItemCommand nestedCommand = new UpdateVocabularyItemCommand(
                    command.word(),
                    command.languageCode(),
                    command.meaning(),
                    command.example(),
                    command.pronunciation(),
                    command.ipa(),
                    command.partOfSpeech(),
                    command.sourceName(),
                    command.sourceUrl(),
                    mapVocabularyLearningStatus(command.learningStatus()),
                    command.nextReviewAt(),
                    command.intervalDays(),
                    command.easeFactor(),
                    command.repetitionCount(),
                    command.lapseCount()
            );
            return mapVocabularyItemView(vocabularyOperations.update(id, nestedCommand));
        } catch (VocabularyItemNotFoundException ex) {
            throw new KnowledgeNotFoundException(ex.getMessage());
        } catch (InvalidVocabularyItemException ex) {
            throw new InvalidKnowledgeItemException(ex.getMessage());
        }
    }

    @Override
    public Optional<KnowledgeVocabularyItemView> findVocabularyItemById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return vocabularyOperations.findById(id).map(this::mapVocabularyItemView);
    }

    @Override
    public KnowledgeVocabularyReviewTransitionResultView reviewVocabularyItem(
            Long id,
            KnowledgeVocabularyReviewTransitionCommand command
    ) {
        if (id == null) {
            throw new InvalidKnowledgeItemException("Vocabulary item ID must not be null");
        }
        if (command == null) {
            throw new InvalidKnowledgeItemException("KnowledgeVocabularyReviewTransitionCommand must not be null");
        }
        try {
            VocabularyReviewTransitionCommand nestedCommand = new VocabularyReviewTransitionCommand(
                    mapSrsReviewResponse(command.response()),
                    mapVocabularyLearningStatus(command.learningStatus()),
                    command.nextReviewAt(),
                    command.newIntervalDays(),
                    command.newEaseFactor(),
                    command.repetitionCount(),
                    command.lapseCount(),
                    command.reviewedAt()
            );
            VocabularyReviewTransitionResultView result = vocabularyOperations.reviewTransition(id, nestedCommand);
            return new KnowledgeVocabularyReviewTransitionResultView(
                    mapVocabularyItemView(result.item()),
                    mapVocabularyReviewView(result.review())
            );
        } catch (VocabularyItemNotFoundException ex) {
            throw new KnowledgeNotFoundException(ex.getMessage());
        } catch (InvalidVocabularyItemException ex) {
            throw new InvalidKnowledgeItemException(ex.getMessage());
        }
    }

    @Override
    public List<KnowledgeVocabularyItemView> findDueVocabularyItems(Instant cutoff, int limit) {
        try {
            return vocabularyOperations.findDue(cutoff, limit)
                    .stream()
                    .map(this::mapVocabularyItemView)
                    .toList();
        } catch (InvalidVocabularyItemException ex) {
            throw new InvalidKnowledgeItemException(ex.getMessage());
        }
    }

    @Override
    public List<KnowledgeVocabularyReviewView> findVocabularyReviews(Long vocabularyId, int limit) {
        try {
            return vocabularyOperations.findReviewsByVocabularyId(vocabularyId, limit)
                    .stream()
                    .map(this::mapVocabularyReviewView)
                    .toList();
        } catch (InvalidVocabularyItemException ex) {
            throw new InvalidKnowledgeItemException(ex.getMessage());
        }
    }

    // Note
    @Override
    public KnowledgeNoteView createNote(CreateKnowledgeNoteCommand command) {
        if (command == null) {
            throw new InvalidKnowledgeItemException("CreateKnowledgeNoteCommand must not be null");
        }
        try {
            CreateNoteCommand nestedCommand = new CreateNoteCommand(
                    command.title(),
                    command.contentMarkdown(),
                    command.summary(),
                    command.sourceName(),
                    command.sourceUrl(),
                    command.importedFileName(),
                    command.importedFileHash(),
                    command.frontmatter()
            );
            return mapNoteView(noteOperations.create(nestedCommand));
        } catch (InvalidNoteException ex) {
            throw new InvalidKnowledgeItemException(ex.getMessage());
        } catch (NoteConflictException ex) {
            throw new KnowledgeConflictException(ex.getMessage());
        }
    }

    @Override
    public KnowledgeNoteView updateNote(Long id, UpdateKnowledgeNoteCommand command) {
        if (id == null) {
            throw new InvalidKnowledgeItemException("Note ID must not be null");
        }
        if (command == null) {
            throw new InvalidKnowledgeItemException("UpdateKnowledgeNoteCommand must not be null");
        }
        try {
            UpdateNoteCommand nestedCommand = new UpdateNoteCommand(
                    command.title(),
                    command.contentMarkdown(),
                    command.summary(),
                    command.sourceName(),
                    command.sourceUrl(),
                    command.importedFileName(),
                    command.importedFileHash(),
                    command.frontmatter()
            );
            return mapNoteView(noteOperations.update(id, nestedCommand));
        } catch (NoteNotFoundException ex) {
            throw new KnowledgeNotFoundException(ex.getMessage());
        } catch (InvalidNoteException ex) {
            throw new InvalidKnowledgeItemException(ex.getMessage());
        } catch (NoteConflictException ex) {
            throw new KnowledgeConflictException(ex.getMessage());
        }
    }

    @Override
    public Optional<KnowledgeNoteView> findNoteById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return noteOperations.findById(id).map(this::mapNoteView);
    }

    @Override
    public Optional<KnowledgeNoteView> findNoteByImportedFileHash(String importedFileHash) {
        if (importedFileHash == null || importedFileHash.isBlank()) {
            return Optional.empty();
        }
        return noteOperations.findByImportedFileHash(importedFileHash).map(this::mapNoteView);
    }

    // Mappings
    private StudyType mapStudyType(KnowledgeStudyType type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case COURSE -> StudyType.COURSE;
            case BOOK -> StudyType.BOOK;
            case GITHUB_REPOSITORY -> StudyType.GITHUB_REPOSITORY;
            case WEBSITE -> StudyType.WEBSITE;
            case YOUTUBE_CHANNEL -> StudyType.YOUTUBE_CHANNEL;
        };
    }

    private KnowledgeStudyType mapStudyType(StudyType type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case COURSE -> KnowledgeStudyType.COURSE;
            case BOOK -> KnowledgeStudyType.BOOK;
            case GITHUB_REPOSITORY -> KnowledgeStudyType.GITHUB_REPOSITORY;
            case WEBSITE -> KnowledgeStudyType.WEBSITE;
            case YOUTUBE_CHANNEL -> KnowledgeStudyType.YOUTUBE_CHANNEL;
        };
    }

    private StudyStatus mapStudyStatus(KnowledgeStudyStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case PLANNED -> StudyStatus.PLANNED;
            case IN_PROGRESS -> StudyStatus.IN_PROGRESS;
            case PAUSED -> StudyStatus.PAUSED;
            case COMPLETED -> StudyStatus.COMPLETED;
        };
    }

    private KnowledgeStudyStatus mapStudyStatus(StudyStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case PLANNED -> KnowledgeStudyStatus.PLANNED;
            case IN_PROGRESS -> KnowledgeStudyStatus.IN_PROGRESS;
            case PAUSED -> KnowledgeStudyStatus.PAUSED;
            case COMPLETED -> KnowledgeStudyStatus.COMPLETED;
        };
    }

    private KnowledgeStudyItemView mapStudyItemView(StudyItemView view) {
        return new KnowledgeStudyItemView(
                view.id(),
                view.title(),
                view.posterUrl(),
                mapStudyType(view.type()),
                view.siteDomain(),
                view.youtubeChannelAccountId(),
                view.authorPersonId(),
                view.authorGroupId(),
                view.publishedDate(),
                view.priceAmount(),
                view.currencyCode(),
                view.description(),
                view.url(),
                view.review(),
                mapStudyStatus(view.learningStatus()),
                view.progressPercent(),
                view.currentProgressText()
        );
    }

    private InformationType mapInformationType(KnowledgeInformationType type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case FINANCE -> InformationType.FINANCE;
            case TECHNOLOGY -> InformationType.TECHNOLOGY;
            case HEALTH -> InformationType.HEALTH;
            case OTHER -> InformationType.OTHER;
        };
    }

    private KnowledgeInformationType mapInformationType(InformationType type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case FINANCE -> KnowledgeInformationType.FINANCE;
            case TECHNOLOGY -> KnowledgeInformationType.TECHNOLOGY;
            case HEALTH -> KnowledgeInformationType.HEALTH;
            case OTHER -> KnowledgeInformationType.OTHER;
        };
    }

    private KnowledgeInformationItemView mapInformationItemView(InformationItemView view) {
        return new KnowledgeInformationItemView(
                view.id(),
                view.title(),
                mapInformationType(view.type()),
                view.description(),
                view.contentMarkdown(),
                view.example(),
                view.sourceName(),
                view.sourceUrl()
        );
    }

    private VocabularyLearningStatus mapVocabularyLearningStatus(KnowledgeVocabularyLearningStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case NEW -> VocabularyLearningStatus.NEW;
            case LEARNING -> VocabularyLearningStatus.LEARNING;
            case REVIEW -> VocabularyLearningStatus.REVIEW;
            case MASTERED -> VocabularyLearningStatus.MASTERED;
        };
    }

    private KnowledgeVocabularyLearningStatus mapVocabularyLearningStatus(VocabularyLearningStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case NEW -> KnowledgeVocabularyLearningStatus.NEW;
            case LEARNING -> KnowledgeVocabularyLearningStatus.LEARNING;
            case REVIEW -> KnowledgeVocabularyLearningStatus.REVIEW;
            case MASTERED -> KnowledgeVocabularyLearningStatus.MASTERED;
        };
    }

    private SrsReviewResponse mapSrsReviewResponse(KnowledgeSrsReviewResponse response) {
        if (response == null) {
            return null;
        }
        return switch (response) {
            case AGAIN -> SrsReviewResponse.AGAIN;
            case HARD -> SrsReviewResponse.HARD;
            case GOOD -> SrsReviewResponse.GOOD;
            case EASY -> SrsReviewResponse.EASY;
        };
    }

    private KnowledgeSrsReviewResponse mapSrsReviewResponse(SrsReviewResponse response) {
        if (response == null) {
            return null;
        }
        return switch (response) {
            case AGAIN -> KnowledgeSrsReviewResponse.AGAIN;
            case HARD -> KnowledgeSrsReviewResponse.HARD;
            case GOOD -> KnowledgeSrsReviewResponse.GOOD;
            case EASY -> KnowledgeSrsReviewResponse.EASY;
        };
    }

    private KnowledgeVocabularyItemView mapVocabularyItemView(VocabularyItemView view) {
        return new KnowledgeVocabularyItemView(
                view.id(),
                view.word(),
                view.languageCode(),
                view.meaning(),
                view.example(),
                view.pronunciation(),
                view.ipa(),
                view.partOfSpeech(),
                view.sourceName(),
                view.sourceUrl(),
                mapVocabularyLearningStatus(view.learningStatus()),
                view.nextReviewAt(),
                view.intervalDays(),
                view.easeFactor(),
                view.repetitionCount(),
                view.lapseCount()
        );
    }

    private KnowledgeVocabularyReviewView mapVocabularyReviewView(VocabularyReviewView view) {
        return new KnowledgeVocabularyReviewView(
                view.id(),
                view.vocabularyId(),
                mapSrsReviewResponse(view.response()),
                view.reviewedAt(),
                view.previousIntervalDays(),
                view.newIntervalDays(),
                view.previousEaseFactor(),
                view.newEaseFactor(),
                view.nextReviewAt()
        );
    }

    private KnowledgeNoteView mapNoteView(NoteView view) {
        return new KnowledgeNoteView(
                view.id(),
                view.title(),
                view.contentMarkdown(),
                view.summary(),
                view.sourceName(),
                view.sourceUrl(),
                view.importedFileName(),
                view.importedFileHash(),
                view.frontmatter()
        );
    }
}
