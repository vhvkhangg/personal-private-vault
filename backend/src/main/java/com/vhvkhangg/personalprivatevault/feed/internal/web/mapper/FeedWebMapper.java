package com.vhvkhangg.personalprivatevault.feed.internal.web.mapper;

import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.ConvertToInformationRequest;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.ConvertToNoteRequest;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.ConvertToStudyRequest;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.CreateFeedSavedResourceRequest;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.CreateFeedSourceRequest;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.CreateManualSavedResourceRequest;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.FeedItemResponse;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.FeedSourceResponse;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.SavedResourceConversionResponse;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.SavedResourceResponse;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.UpdateFeedSourceRequest;
import com.vhvkhangg.personalprivatevault.feed.resource.command.CreateFeedSavedResourceCommand;
import com.vhvkhangg.personalprivatevault.feed.resource.command.CreateManualSavedResourceCommand;
import com.vhvkhangg.personalprivatevault.feed.source.command.CreateFeedSourceCommand;
import com.vhvkhangg.personalprivatevault.feed.source.command.UpdateFeedSourceCommand;
import com.vhvkhangg.personalprivatevault.feed.view.FeedItemView;
import com.vhvkhangg.personalprivatevault.feed.view.FeedSourceView;
import com.vhvkhangg.personalprivatevault.feed.view.SavedResourceConversionView;
import com.vhvkhangg.personalprivatevault.feed.view.SavedResourceView;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeInformationItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeNoteCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeStudyItemCommand;

public final class FeedWebMapper {

    private FeedWebMapper() {}

    public static CreateFeedSourceCommand toCommand(CreateFeedSourceRequest request) {
        return new CreateFeedSourceCommand(
                request.name(),
                request.type(),
                request.sourceUrl(),
                request.feedUrl(),
                request.enabled(),
                request.scheduledRefreshEnabled(),
                request.refreshIntervalMinutes(),
                request.config()
        );
    }

    public static UpdateFeedSourceCommand toCommand(UpdateFeedSourceRequest request) {
        return new UpdateFeedSourceCommand(
                request.name(),
                request.type(),
                request.sourceUrl(),
                request.feedUrl(),
                request.enabled(),
                request.scheduledRefreshEnabled(),
                request.refreshIntervalMinutes(),
                request.config()
        );
    }

    public static FeedSourceResponse toResponse(FeedSourceView view) {
        return new FeedSourceResponse(
                view.id(),
                view.name(),
                view.type(),
                view.sourceUrl(),
                view.feedUrl(),
                view.enabled(),
                view.scheduledRefreshEnabled(),
                view.refreshIntervalMinutes(),
                view.config(),
                view.lastFetchedAt(),
                view.nextFetchAt(),
                view.createdAt(),
                view.updatedAt()
        );
    }

    public static FeedItemResponse toResponse(FeedItemView view) {
        return new FeedItemResponse(
                view.id(),
                view.feedSourceId(),
                view.externalId(),
                view.title(),
                view.url(),
                view.urlHash(),
                view.author(),
                view.summary(),
                view.publishedAt(),
                view.fetchedAt(),
                view.rawMetadata()
        );
    }

    public static CreateFeedSavedResourceCommand toCommand(CreateFeedSavedResourceRequest request) {
        return new CreateFeedSavedResourceCommand(
                request.feedItemId(),
                request.kind()
        );
    }

    public static CreateManualSavedResourceCommand toCommand(CreateManualSavedResourceRequest request) {
        return new CreateManualSavedResourceCommand(
                request.kind(),
                request.title(),
                request.resourceUrl(),
                request.author(),
                request.summary(),
                request.publishedAt(),
                request.sourceName(),
                request.sourceUrl(),
                request.externalId(),
                request.rawMetadata()
        );
    }

    public static SavedResourceResponse toResponse(SavedResourceView view) {
        return new SavedResourceResponse(
                view.id(),
                view.feedItemId(),
                view.kind(),
                view.sourceName(),
                view.sourceUrl(),
                view.externalId(),
                view.title(),
                view.resourceUrl(),
                view.resourceUrlHash(),
                view.author(),
                view.summary(),
                view.publishedAt(),
                view.fetchedAt(),
                view.savedAt(),
                view.rawMetadata()
        );
    }

    public static CreateKnowledgeStudyItemCommand toStudyCommand(ConvertToStudyRequest request) {
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

    public static CreateKnowledgeInformationItemCommand toInformationCommand(ConvertToInformationRequest request) {
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

    public static CreateKnowledgeNoteCommand toNoteCommand(ConvertToNoteRequest request) {
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

    public static SavedResourceConversionResponse toResponse(SavedResourceConversionView view) {
        return new SavedResourceConversionResponse(
                view.savedResourceId(),
                view.targetVaultEntryId(),
                view.createdAt()
        );
    }
}
