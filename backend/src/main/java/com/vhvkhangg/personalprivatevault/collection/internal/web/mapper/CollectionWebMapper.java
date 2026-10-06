package com.vhvkhangg.personalprivatevault.collection.internal.web.mapper;

import com.vhvkhangg.personalprivatevault.collection.api.CollectionMusicCreditView;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionMusicView;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionShoppingItemView;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionSoftwareItemView;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionSoftwarePlatformView;
import com.vhvkhangg.personalprivatevault.collection.api.CreateCollectionMusicCommand;
import com.vhvkhangg.personalprivatevault.collection.api.CreateCollectionShoppingItemCommand;
import com.vhvkhangg.personalprivatevault.collection.api.CreateCollectionSoftwareItemCommand;
import com.vhvkhangg.personalprivatevault.collection.api.UpdateCollectionMusicCommand;
import com.vhvkhangg.personalprivatevault.collection.api.UpdateCollectionShoppingItemCommand;
import com.vhvkhangg.personalprivatevault.collection.api.UpdateCollectionSoftwareItemCommand;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.CreateMusicRequest;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.CreateShoppingItemRequest;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.CreateSoftwareItemRequest;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.MusicCreditResponse;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.MusicResponse;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.ShoppingItemResponse;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.SoftwareItemResponse;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.SoftwarePlatformResponse;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.UpdateMusicRequest;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.UpdateShoppingItemRequest;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.UpdateSoftwareItemRequest;

public final class CollectionWebMapper {

    private CollectionWebMapper() {}

    public static CreateCollectionMusicCommand toCommand(CreateMusicRequest request) {
        return new CreateCollectionMusicCommand(
                request.title(),
                request.version(),
                request.platformId(),
                request.url()
        );
    }

    public static UpdateCollectionMusicCommand toCommand(UpdateMusicRequest request) {
        return new UpdateCollectionMusicCommand(
                request.title(),
                request.version(),
                request.platformId(),
                request.url()
        );
    }

    public static MusicResponse toResponse(CollectionMusicView view) {
        return new MusicResponse(
                view.id(),
                view.title(),
                view.version(),
                view.platformId(),
                view.url()
        );
    }

    public static MusicCreditResponse toResponse(CollectionMusicCreditView view) {
        return new MusicCreditResponse(
                view.musicId(),
                view.personId(),
                view.role()
        );
    }

    public static CreateCollectionShoppingItemCommand toCommand(CreateShoppingItemRequest request) {
        return new CreateCollectionShoppingItemCommand(
                request.name(),
                request.avatarUrl(),
                request.description(),
                request.priceAmount(),
                request.currencyCode(),
                request.platformId(),
                request.status(),
                request.url(),
                request.purchasedAt()
        );
    }

    public static UpdateCollectionShoppingItemCommand toCommand(UpdateShoppingItemRequest request) {
        return new UpdateCollectionShoppingItemCommand(
                request.name(),
                request.avatarUrl(),
                request.description(),
                request.priceAmount(),
                request.currencyCode(),
                request.platformId(),
                request.status(),
                request.url(),
                request.purchasedAt()
        );
    }

    public static ShoppingItemResponse toResponse(CollectionShoppingItemView view) {
        return new ShoppingItemResponse(
                view.id(),
                view.name(),
                view.avatarUrl(),
                view.description(),
                view.priceAmount(),
                view.currencyCode(),
                view.platformId(),
                view.status(),
                view.url(),
                view.purchasedAt()
        );
    }

    public static CreateCollectionSoftwareItemCommand toCommand(CreateSoftwareItemRequest request) {
        return new CreateCollectionSoftwareItemCommand(
                request.name(),
                request.type(),
                request.logoUrl(),
                request.description(),
                request.priceAmount(),
                request.currencyCode(),
                request.url(),
                request.review()
        );
    }

    public static UpdateCollectionSoftwareItemCommand toCommand(UpdateSoftwareItemRequest request) {
        return new UpdateCollectionSoftwareItemCommand(
                request.name(),
                request.type(),
                request.logoUrl(),
                request.description(),
                request.priceAmount(),
                request.currencyCode(),
                request.url(),
                request.review()
        );
    }

    public static SoftwareItemResponse toResponse(CollectionSoftwareItemView view) {
        return new SoftwareItemResponse(
                view.id(),
                view.name(),
                view.type(),
                view.logoUrl(),
                view.description(),
                view.priceAmount(),
                view.currencyCode(),
                view.url(),
                view.review()
        );
    }

    public static SoftwarePlatformResponse toResponse(CollectionSoftwarePlatformView view) {
        return new SoftwarePlatformResponse(
                view.softwareId(),
                view.platformId()
        );
    }
}
