package com.vhvkhangg.personalprivatevault.knowledge.internal.web.mapper;

import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeInformationItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeNoteCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeStudyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeVocabularyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeInformationItemView;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeNoteView;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeStudyItemView;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeVocabularyItemView;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeVocabularyReviewTransitionCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeVocabularyReviewTransitionResultView;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeVocabularyReviewView;
import com.vhvkhangg.personalprivatevault.knowledge.api.UpdateKnowledgeInformationItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.UpdateKnowledgeNoteCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.UpdateKnowledgeStudyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.UpdateKnowledgeVocabularyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.CreateKnowledgeInformationRequest;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.CreateKnowledgeNoteRequest;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.CreateKnowledgeStudyRequest;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.CreateKnowledgeVocabularyRequest;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.KnowledgeInformationResponse;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.KnowledgeNoteResponse;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.KnowledgeStudyResponse;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.KnowledgeVocabularyResponse;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.KnowledgeVocabularyReviewResponse;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.UpdateKnowledgeInformationRequest;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.UpdateKnowledgeNoteRequest;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.UpdateKnowledgeStudyRequest;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.UpdateKnowledgeVocabularyRequest;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.VocabularyReviewRequest;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.VocabularyReviewResultResponse;

public final class KnowledgeWebMapper {

    private KnowledgeWebMapper() {}

    public static KnowledgeStudyResponse toResponse(KnowledgeStudyItemView view) {
        if (view == null) return null;
        return new KnowledgeStudyResponse(
                view.id(),
                view.title(),
                view.posterUrl(),
                view.type(),
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
                view.learningStatus(),
                view.progressPercent(),
                view.currentProgressText()
        );
    }

    public static CreateKnowledgeStudyItemCommand toCommand(CreateKnowledgeStudyRequest request) {
        if (request == null) return null;
        return new CreateKnowledgeStudyItemCommand(
                request.title(),
                request.posterUrl(),
                request.type(),
                request.siteDomain(),
                request.youtubeChannelAccountId(),
                request.authorPersonId(),
                request.authorGroupId(),
                request.publishedDate(),
                request.priceAmount(),
                request.currencyCode(),
                request.description(),
                request.url(),
                request.review(),
                request.learningStatus(),
                request.progressPercent(),
                request.currentProgressText()
        );
    }

    public static UpdateKnowledgeStudyItemCommand toCommand(UpdateKnowledgeStudyRequest request) {
        if (request == null) return null;
        return new UpdateKnowledgeStudyItemCommand(
                request.title(),
                request.posterUrl(),
                request.type(),
                request.siteDomain(),
                request.youtubeChannelAccountId(),
                request.authorPersonId(),
                request.authorGroupId(),
                request.publishedDate(),
                request.priceAmount(),
                request.currencyCode(),
                request.description(),
                request.url(),
                request.review(),
                request.learningStatus(),
                request.progressPercent(),
                request.currentProgressText()
        );
    }

    public static KnowledgeInformationResponse toResponse(KnowledgeInformationItemView view) {
        if (view == null) return null;
        return new KnowledgeInformationResponse(
                view.id(),
                view.title(),
                view.type(),
                view.description(),
                view.contentMarkdown(),
                view.example(),
                view.sourceName(),
                view.sourceUrl()
        );
    }

    public static CreateKnowledgeInformationItemCommand toCommand(CreateKnowledgeInformationRequest request) {
        if (request == null) return null;
        return new CreateKnowledgeInformationItemCommand(
                request.title(),
                request.type(),
                request.description(),
                request.contentMarkdown(),
                request.example(),
                request.sourceName(),
                request.sourceUrl()
        );
    }

    public static UpdateKnowledgeInformationItemCommand toCommand(UpdateKnowledgeInformationRequest request) {
        if (request == null) return null;
        return new UpdateKnowledgeInformationItemCommand(
                request.title(),
                request.type(),
                request.description(),
                request.contentMarkdown(),
                request.example(),
                request.sourceName(),
                request.sourceUrl()
        );
    }

    public static KnowledgeVocabularyResponse toResponse(KnowledgeVocabularyItemView view) {
        if (view == null) return null;
        return new KnowledgeVocabularyResponse(
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
                view.learningStatus(),
                view.nextReviewAt(),
                view.intervalDays(),
                view.easeFactor(),
                view.repetitionCount(),
                view.lapseCount()
        );
    }

    public static CreateKnowledgeVocabularyItemCommand toCommand(CreateKnowledgeVocabularyRequest request) {
        if (request == null) return null;
        return new CreateKnowledgeVocabularyItemCommand(
                request.word(),
                request.languageCode(),
                request.meaning(),
                request.example(),
                request.pronunciation(),
                request.ipa(),
                request.partOfSpeech(),
                request.sourceName(),
                request.sourceUrl(),
                request.learningStatus() != null ? request.learningStatus() : com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeVocabularyLearningStatus.NEW,
                request.nextReviewAt(),
                request.intervalDays() != null ? request.intervalDays() : 0,
                request.easeFactor() != null ? request.easeFactor() : new java.math.BigDecimal("2.50"),
                request.repetitionCount() != null ? request.repetitionCount() : 0,
                request.lapseCount() != null ? request.lapseCount() : 0
        );
    }

    public static UpdateKnowledgeVocabularyItemCommand toCommand(UpdateKnowledgeVocabularyRequest request) {
        if (request == null) return null;
        return new UpdateKnowledgeVocabularyItemCommand(
                request.word(),
                request.languageCode(),
                request.meaning(),
                request.example(),
                request.pronunciation(),
                request.ipa(),
                request.partOfSpeech(),
                request.sourceName(),
                request.sourceUrl(),
                request.learningStatus() != null ? request.learningStatus() : com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeVocabularyLearningStatus.NEW,
                request.nextReviewAt(),
                request.intervalDays() != null ? request.intervalDays() : 0,
                request.easeFactor() != null ? request.easeFactor() : new java.math.BigDecimal("2.50"),
                request.repetitionCount() != null ? request.repetitionCount() : 0,
                request.lapseCount() != null ? request.lapseCount() : 0
        );
    }

    public static KnowledgeVocabularyReviewResponse toResponse(KnowledgeVocabularyReviewView view) {
        if (view == null) return null;
        return new KnowledgeVocabularyReviewResponse(
                view.id(),
                view.vocabularyId(),
                view.response(),
                view.reviewedAt(),
                view.previousIntervalDays(),
                view.newIntervalDays(),
                view.previousEaseFactor(),
                view.newEaseFactor(),
                view.nextReviewAt()
        );
    }

    public static KnowledgeVocabularyReviewTransitionCommand toCommand(VocabularyReviewRequest request) {
        if (request == null) return null;
        return new KnowledgeVocabularyReviewTransitionCommand(
                request.response(),
                request.learningStatus(),
                request.nextReviewAt(),
                request.newIntervalDays(),
                request.newEaseFactor(),
                request.repetitionCount(),
                request.lapseCount(),
                request.reviewedAt()
        );
    }

    public static VocabularyReviewResultResponse toResponse(KnowledgeVocabularyReviewTransitionResultView view) {
        if (view == null) return null;
        return new VocabularyReviewResultResponse(
                toResponse(view.item()),
                toResponse(view.review())
        );
    }

    public static KnowledgeNoteResponse toResponse(KnowledgeNoteView view) {
        if (view == null) return null;
        return new KnowledgeNoteResponse(
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

    public static CreateKnowledgeNoteCommand toCommand(CreateKnowledgeNoteRequest request) {
        if (request == null) return null;
        return new CreateKnowledgeNoteCommand(
                request.title(),
                request.contentMarkdown(),
                request.summary(),
                request.sourceName(),
                request.sourceUrl(),
                request.importedFileName(),
                request.importedFileHash(),
                request.frontmatter()
        );
    }

    public static UpdateKnowledgeNoteCommand toCommand(UpdateKnowledgeNoteRequest request) {
        if (request == null) return null;
        return new UpdateKnowledgeNoteCommand(
                request.title(),
                request.contentMarkdown(),
                request.summary(),
                request.sourceName(),
                request.sourceUrl(),
                request.importedFileName(),
                request.importedFileHash(),
                request.frontmatter()
        );
    }
}
