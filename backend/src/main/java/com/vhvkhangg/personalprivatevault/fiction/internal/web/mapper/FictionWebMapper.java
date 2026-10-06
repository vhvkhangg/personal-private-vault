package com.vhvkhangg.personalprivatevault.fiction.internal.web.mapper;

import com.vhvkhangg.personalprivatevault.fiction.fiction.command.CreateFictionCommand;
import com.vhvkhangg.personalprivatevault.fiction.fiction.command.UpdateFictionCommand;
import com.vhvkhangg.personalprivatevault.fiction.genre.command.CreateFictionGenreCommand;
import com.vhvkhangg.personalprivatevault.fiction.genre.command.UpdateFictionGenreCommand;
import com.vhvkhangg.personalprivatevault.fiction.internal.web.dto.CreateFictionGenreRequest;
import com.vhvkhangg.personalprivatevault.fiction.internal.web.dto.CreateFictionLinkRequest;
import com.vhvkhangg.personalprivatevault.fiction.internal.web.dto.CreateFictionRequest;
import com.vhvkhangg.personalprivatevault.fiction.internal.web.dto.FictionClassificationsResponse;
import com.vhvkhangg.personalprivatevault.fiction.internal.web.dto.FictionGenreResponse;
import com.vhvkhangg.personalprivatevault.fiction.internal.web.dto.FictionLinkResponse;
import com.vhvkhangg.personalprivatevault.fiction.internal.web.dto.FictionResponse;
import com.vhvkhangg.personalprivatevault.fiction.internal.web.dto.UpdateFictionGenreRequest;
import com.vhvkhangg.personalprivatevault.fiction.internal.web.dto.UpdateFictionLinkRequest;
import com.vhvkhangg.personalprivatevault.fiction.internal.web.dto.UpdateFictionRequest;
import com.vhvkhangg.personalprivatevault.fiction.link.command.CreateFictionLinkCommand;
import com.vhvkhangg.personalprivatevault.fiction.link.command.UpdateFictionLinkCommand;
import com.vhvkhangg.personalprivatevault.fiction.view.FictionClassificationsView;
import com.vhvkhangg.personalprivatevault.fiction.view.FictionGenreView;
import com.vhvkhangg.personalprivatevault.fiction.view.FictionLinkView;
import com.vhvkhangg.personalprivatevault.fiction.view.FictionView;

public final class FictionWebMapper {

    private FictionWebMapper() {}

    public static FictionResponse toResponse(FictionView view) {
        if (view == null) return null;
        return new FictionResponse(
                view.id(),
                view.title(),
                view.originalTitle(),
                view.nationalityCode(),
                view.posterUrl(),
                view.format(),
                view.isNsfw(),
                view.genreId(),
                view.authorPersonId(),
                view.authorGroupId(),
                view.description(),
                view.totalChapters(),
                view.progressStatus(),
                view.consumptionStatus(),
                view.currentProgressText(),
                view.review(),
                view.storyArchetypeIds(),
                view.worldSettingIds()
        );
    }

    public static CreateFictionCommand toCommand(CreateFictionRequest request) {
        if (request == null) return null;
        return new CreateFictionCommand(
                request.title(),
                request.originalTitle(),
                request.nationalityCode(),
                request.posterUrl(),
                request.format(),
                request.isNsfw(),
                request.genreId(),
                request.authorPersonId(),
                request.authorGroupId(),
                request.description(),
                request.totalChapters(),
                request.progressStatus(),
                request.consumptionStatus(),
                request.currentProgressText(),
                request.review(),
                request.storyArchetypeIds() != null ? request.storyArchetypeIds() : java.util.Set.of(),
                request.worldSettingIds() != null ? request.worldSettingIds() : java.util.Set.of()
        );
    }

    public static UpdateFictionCommand toCommand(Long id, UpdateFictionRequest request) {
        if (request == null) return null;
        return new UpdateFictionCommand(
                id,
                request.title(),
                request.originalTitle(),
                request.nationalityCode(),
                request.posterUrl(),
                request.format(),
                request.isNsfw(),
                request.genreId(),
                request.authorPersonId(),
                request.authorGroupId(),
                request.description(),
                request.totalChapters(),
                request.progressStatus(),
                request.consumptionStatus(),
                request.currentProgressText(),
                request.review()
        );
    }

    public static FictionClassificationsResponse toResponse(FictionClassificationsView view) {
        if (view == null) return null;
        return new FictionClassificationsResponse(
                view.fictionId(),
                view.storyArchetypeIds(),
                view.worldSettingIds()
        );
    }

    public static FictionGenreResponse toResponse(FictionGenreView view) {
        if (view == null) return null;
        return new FictionGenreResponse(view.id(), view.name(), view.description());
    }

    public static CreateFictionGenreCommand toCommand(CreateFictionGenreRequest request) {
        if (request == null) return null;
        return new CreateFictionGenreCommand(request.name(), request.description());
    }

    public static UpdateFictionGenreCommand toCommand(Long id, UpdateFictionGenreRequest request) {
        if (request == null) return null;
        return new UpdateFictionGenreCommand(id, request.name(), request.description());
    }

    public static FictionLinkResponse toResponse(FictionLinkView view) {
        if (view == null) return null;
        return new FictionLinkResponse(
                view.id(),
                view.fictionId(),
                view.languageCode(),
                view.linkType(),
                view.label(),
                view.url(),
                view.isPrimary(),
                view.createdAt()
        );
    }

    public static CreateFictionLinkCommand toCommand(Long fictionId, CreateFictionLinkRequest request) {
        if (request == null) return null;
        return new CreateFictionLinkCommand(
                fictionId,
                request.languageCode(),
                request.linkType(),
                request.label(),
                request.url(),
                request.isPrimary()
        );
    }

    public static UpdateFictionLinkCommand toCommand(Long id, Long fictionId, UpdateFictionLinkRequest request) {
        if (request == null) return null;
        return new UpdateFictionLinkCommand(
                id,
                fictionId,
                request.languageCode(),
                request.linkType(),
                request.label(),
                request.url(),
                request.isPrimary()
        );
    }
}
