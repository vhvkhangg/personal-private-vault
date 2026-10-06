package com.vhvkhangg.personalprivatevault.film.internal.web.mapper;

import com.vhvkhangg.personalprivatevault.film.credit.command.CreateFilmCreditCommand;
import com.vhvkhangg.personalprivatevault.film.film.command.CreateFilmCommand;
import com.vhvkhangg.personalprivatevault.film.film.command.UpdateFilmCommand;
import com.vhvkhangg.personalprivatevault.film.genre.command.CreateFilmGenreCommand;
import com.vhvkhangg.personalprivatevault.film.genre.command.UpdateFilmGenreCommand;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.CreateFilmCreditRequest;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.CreateFilmGenreRequest;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.CreateFilmLinkRequest;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.CreateFilmRequest;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.FilmClassificationsResponse;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.FilmCreditResponse;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.FilmGenreResponse;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.FilmLinkResponse;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.FilmResponse;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.UpdateFilmGenreRequest;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.UpdateFilmLinkRequest;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.UpdateFilmRequest;
import com.vhvkhangg.personalprivatevault.film.link.command.CreateFilmLinkCommand;
import com.vhvkhangg.personalprivatevault.film.link.command.UpdateFilmLinkCommand;
import com.vhvkhangg.personalprivatevault.film.view.FilmClassificationsView;
import com.vhvkhangg.personalprivatevault.film.view.FilmCreditView;
import com.vhvkhangg.personalprivatevault.film.view.FilmGenreView;
import com.vhvkhangg.personalprivatevault.film.view.FilmLinkView;
import com.vhvkhangg.personalprivatevault.film.view.FilmView;

public final class FilmWebMapper {

    private FilmWebMapper() {}

    public static FilmResponse toResponse(FilmView view) {
        if (view == null) return null;
        return new FilmResponse(
                view.id(),
                view.title(),
                view.originalTitle(),
                view.nationalityCode(),
                view.posterUrl(),
                view.format(),
                view.productionStyle(),
                view.isNsfw(),
                view.directorPersonId(),
                view.description(),
                view.totalEpisodes(),
                view.progressStatus(),
                view.consumptionStatus(),
                view.currentProgressText(),
                view.review(),
                view.genreIds(),
                view.storyArchetypeIds(),
                view.worldSettingIds()
        );
    }

    public static CreateFilmCommand toCommand(CreateFilmRequest request) {
        if (request == null) return null;
        return new CreateFilmCommand(
                request.title(),
                request.originalTitle(),
                request.nationalityCode(),
                request.posterUrl(),
                request.format(),
                request.productionStyle(),
                request.isNsfw(),
                request.directorPersonId(),
                request.description(),
                request.totalEpisodes(),
                request.progressStatus(),
                request.consumptionStatus(),
                request.currentProgressText(),
                request.review(),
                request.genreIds() != null ? request.genreIds() : java.util.Set.of(),
                request.storyArchetypeIds() != null ? request.storyArchetypeIds() : java.util.Set.of(),
                request.worldSettingIds() != null ? request.worldSettingIds() : java.util.Set.of()
        );
    }

    public static UpdateFilmCommand toCommand(Long id, UpdateFilmRequest request) {
        if (request == null) return null;
        return new UpdateFilmCommand(
                id,
                request.title(),
                request.originalTitle(),
                request.nationalityCode(),
                request.posterUrl(),
                request.format(),
                request.productionStyle(),
                request.isNsfw(),
                request.directorPersonId(),
                request.description(),
                request.totalEpisodes(),
                request.progressStatus(),
                request.consumptionStatus(),
                request.currentProgressText(),
                request.review()
        );
    }

    public static FilmClassificationsResponse toResponse(FilmClassificationsView view) {
        if (view == null) return null;
        return new FilmClassificationsResponse(
                view.filmId(),
                view.genreIds(),
                view.storyArchetypeIds(),
                view.worldSettingIds()
        );
    }

    public static FilmCreditResponse toResponse(FilmCreditView view) {
        if (view == null) return null;
        return new FilmCreditResponse(
                view.id(),
                view.filmId(),
                view.personId(),
                view.role(),
                view.characterName(),
                view.note()
        );
    }

    public static CreateFilmCreditCommand toCommand(Long filmId, CreateFilmCreditRequest request) {
        if (request == null) return null;
        return new CreateFilmCreditCommand(
                filmId,
                request.personId(),
                request.role(),
                request.characterName(),
                request.note()
        );
    }

    public static FilmGenreResponse toResponse(FilmGenreView view) {
        if (view == null) return null;
        return new FilmGenreResponse(view.id(), view.name(), view.description());
    }

    public static CreateFilmGenreCommand toCommand(CreateFilmGenreRequest request) {
        if (request == null) return null;
        return new CreateFilmGenreCommand(request.name(), request.description());
    }

    public static UpdateFilmGenreCommand toCommand(Long id, UpdateFilmGenreRequest request) {
        if (request == null) return null;
        return new UpdateFilmGenreCommand(id, request.name(), request.description());
    }

    public static FilmLinkResponse toResponse(FilmLinkView view) {
        if (view == null) return null;
        return new FilmLinkResponse(
                view.id(),
                view.filmId(),
                view.languageCode(),
                view.label(),
                view.url(),
                view.isPrimary(),
                view.createdAt()
        );
    }

    public static CreateFilmLinkCommand toCommand(Long filmId, CreateFilmLinkRequest request) {
        if (request == null) return null;
        return new CreateFilmLinkCommand(
                filmId,
                request.languageCode(),
                request.label(),
                request.url(),
                request.isPrimary()
        );
    }

    public static UpdateFilmLinkCommand toCommand(Long id, Long filmId, UpdateFilmLinkRequest request) {
        if (request == null) return null;
        return new UpdateFilmLinkCommand(
                id,
                filmId,
                request.languageCode(),
                request.label(),
                request.url(),
                request.isPrimary()
        );
    }
}
