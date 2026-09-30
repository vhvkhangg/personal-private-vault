package com.vhvkhangg.personalprivatevault.collection.internal.application;

import com.vhvkhangg.personalprivatevault.collection.api.CollectionMusicCreditRole;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionMusicCreditView;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionMusicVersion;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionMusicView;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionNotFoundException;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionOperations;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionShoppingItemView;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionShoppingStatus;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionSoftwareItemView;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionSoftwarePlatformView;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionSoftwareType;
import com.vhvkhangg.personalprivatevault.collection.api.CreateCollectionMusicCommand;
import com.vhvkhangg.personalprivatevault.collection.api.CreateCollectionShoppingItemCommand;
import com.vhvkhangg.personalprivatevault.collection.api.CreateCollectionSoftwareItemCommand;
import com.vhvkhangg.personalprivatevault.collection.api.InvalidCollectionException;
import com.vhvkhangg.personalprivatevault.collection.api.UpdateCollectionMusicCommand;
import com.vhvkhangg.personalprivatevault.collection.api.UpdateCollectionShoppingItemCommand;
import com.vhvkhangg.personalprivatevault.collection.api.UpdateCollectionSoftwareItemCommand;
import com.vhvkhangg.personalprivatevault.collection.music.enums.MusicCreditRole;
import com.vhvkhangg.personalprivatevault.collection.music.enums.MusicVersion;
import com.vhvkhangg.personalprivatevault.collection.music.music.CreateMusicCommand;
import com.vhvkhangg.personalprivatevault.collection.music.music.InvalidMusicException;
import com.vhvkhangg.personalprivatevault.collection.music.music.MusicNotFoundException;
import com.vhvkhangg.personalprivatevault.collection.music.music.MusicOperations;
import com.vhvkhangg.personalprivatevault.collection.music.music.UpdateMusicCommand;
import com.vhvkhangg.personalprivatevault.collection.music.view.MusicCreditView;
import com.vhvkhangg.personalprivatevault.collection.music.view.MusicView;
import com.vhvkhangg.personalprivatevault.collection.shopping.enums.ShoppingStatus;
import com.vhvkhangg.personalprivatevault.collection.shopping.shopping.CreateShoppingItemCommand;
import com.vhvkhangg.personalprivatevault.collection.shopping.shopping.InvalidShoppingItemException;
import com.vhvkhangg.personalprivatevault.collection.shopping.shopping.ShoppingItemNotFoundException;
import com.vhvkhangg.personalprivatevault.collection.shopping.shopping.ShoppingOperations;
import com.vhvkhangg.personalprivatevault.collection.shopping.shopping.UpdateShoppingItemCommand;
import com.vhvkhangg.personalprivatevault.collection.shopping.view.ShoppingItemView;
import com.vhvkhangg.personalprivatevault.collection.software.enums.SoftwareType;
import com.vhvkhangg.personalprivatevault.collection.software.software.CreateSoftwareItemCommand;
import com.vhvkhangg.personalprivatevault.collection.software.software.InvalidSoftwareItemException;
import com.vhvkhangg.personalprivatevault.collection.software.software.SoftwareItemNotFoundException;
import com.vhvkhangg.personalprivatevault.collection.software.software.SoftwareOperations;
import com.vhvkhangg.personalprivatevault.collection.software.software.UpdateSoftwareItemCommand;
import com.vhvkhangg.personalprivatevault.collection.software.view.SoftwareItemView;
import com.vhvkhangg.personalprivatevault.collection.software.view.SoftwarePlatformView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Closed parent facade service implementing {@link CollectionOperations}.
 * Delegates directly to nested module capabilities with 1-to-1 DTO mapping
 * and exception translation.
 */
@Service
@RequiredArgsConstructor
public class CollectionService implements CollectionOperations {

    private final MusicOperations musicOperations;
    private final ShoppingOperations shoppingOperations;
    private final SoftwareOperations softwareOperations;

    // =========================================================================
    // Music Delegation
    // =========================================================================

    @Override
    public CollectionMusicView createMusic(CreateCollectionMusicCommand command) {
        if (command == null) {
            throw new InvalidCollectionException("CreateCollectionMusicCommand must not be null");
        }
        try {
            CreateMusicCommand nestedCommand = new CreateMusicCommand(
                    command.title(),
                    toNested(command.version()),
                    command.platformId(),
                    command.url()
            );
            return toParent(musicOperations.create(nestedCommand));
        } catch (InvalidMusicException e) {
            throw new InvalidCollectionException(e.getMessage(), e);
        }
    }

    @Override
    public CollectionMusicView updateMusic(Long id, UpdateCollectionMusicCommand command) {
        if (command == null) {
            throw new InvalidCollectionException("UpdateCollectionMusicCommand must not be null");
        }
        try {
            UpdateMusicCommand nestedCommand = new UpdateMusicCommand(
                    command.title(),
                    toNested(command.version()),
                    command.platformId(),
                    command.url()
            );
            return toParent(musicOperations.update(id, nestedCommand));
        } catch (MusicNotFoundException e) {
            throw new CollectionNotFoundException(e.getMessage(), e);
        } catch (InvalidMusicException e) {
            throw new InvalidCollectionException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<CollectionMusicView> findMusicById(Long id) {
        return musicOperations.findById(id).map(this::toParent);
    }

    @Override
    public void addMusicCredit(Long musicId, Long personId, CollectionMusicCreditRole role) {
        try {
            musicOperations.addCredit(musicId, personId, toNested(role));
        } catch (MusicNotFoundException e) {
            throw new CollectionNotFoundException(e.getMessage(), e);
        } catch (InvalidMusicException e) {
            throw new InvalidCollectionException(e.getMessage(), e);
        }
    }

    @Override
    public List<CollectionMusicCreditView> findMusicCredits(Long musicId, int limit) {
        try {
            return musicOperations.findCredits(musicId, limit).stream()
                    .map(this::toParent)
                    .toList();
        } catch (MusicNotFoundException e) {
            throw new CollectionNotFoundException(e.getMessage(), e);
        } catch (InvalidMusicException e) {
            throw new InvalidCollectionException(e.getMessage(), e);
        }
    }

    // =========================================================================
    // Shopping Delegation
    // =========================================================================

    @Override
    public CollectionShoppingItemView createShoppingItem(CreateCollectionShoppingItemCommand command) {
        if (command == null) {
            throw new InvalidCollectionException("CreateCollectionShoppingItemCommand must not be null");
        }
        try {
            CreateShoppingItemCommand nestedCommand = new CreateShoppingItemCommand(
                    command.name(),
                    command.avatarUrl(),
                    command.description(),
                    command.priceAmount(),
                    command.currencyCode(),
                    command.platformId(),
                    toNested(command.status()),
                    command.url(),
                    command.purchasedAt()
            );
            return toParent(shoppingOperations.create(nestedCommand));
        } catch (InvalidShoppingItemException e) {
            throw new InvalidCollectionException(e.getMessage(), e);
        }
    }

    @Override
    public CollectionShoppingItemView updateShoppingItem(Long id, UpdateCollectionShoppingItemCommand command) {
        if (command == null) {
            throw new InvalidCollectionException("UpdateCollectionShoppingItemCommand must not be null");
        }
        try {
            UpdateShoppingItemCommand nestedCommand = new UpdateShoppingItemCommand(
                    command.name(),
                    command.avatarUrl(),
                    command.description(),
                    command.priceAmount(),
                    command.currencyCode(),
                    command.platformId(),
                    toNested(command.status()),
                    command.url(),
                    command.purchasedAt()
            );
            return toParent(shoppingOperations.update(id, nestedCommand));
        } catch (ShoppingItemNotFoundException e) {
            throw new CollectionNotFoundException(e.getMessage(), e);
        } catch (InvalidShoppingItemException e) {
            throw new InvalidCollectionException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<CollectionShoppingItemView> findShoppingItemById(Long id) {
        return shoppingOperations.findById(id).map(this::toParent);
    }

    // =========================================================================
    // Software Delegation
    // =========================================================================

    @Override
    public CollectionSoftwareItemView createSoftwareItem(CreateCollectionSoftwareItemCommand command) {
        if (command == null) {
            throw new InvalidCollectionException("CreateCollectionSoftwareItemCommand must not be null");
        }
        try {
            CreateSoftwareItemCommand nestedCommand = new CreateSoftwareItemCommand(
                    command.name(),
                    toNested(command.type()),
                    command.logoUrl(),
                    command.description(),
                    command.priceAmount(),
                    command.currencyCode(),
                    command.url(),
                    command.review()
            );
            return toParent(softwareOperations.create(nestedCommand));
        } catch (InvalidSoftwareItemException e) {
            throw new InvalidCollectionException(e.getMessage(), e);
        }
    }

    @Override
    public CollectionSoftwareItemView updateSoftwareItem(Long id, UpdateCollectionSoftwareItemCommand command) {
        if (command == null) {
            throw new InvalidCollectionException("UpdateCollectionSoftwareItemCommand must not be null");
        }
        try {
            UpdateSoftwareItemCommand nestedCommand = new UpdateSoftwareItemCommand(
                    command.name(),
                    toNested(command.type()),
                    command.logoUrl(),
                    command.description(),
                    command.priceAmount(),
                    command.currencyCode(),
                    command.url(),
                    command.review()
            );
            return toParent(softwareOperations.update(id, nestedCommand));
        } catch (SoftwareItemNotFoundException e) {
            throw new CollectionNotFoundException(e.getMessage(), e);
        } catch (InvalidSoftwareItemException e) {
            throw new InvalidCollectionException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<CollectionSoftwareItemView> findSoftwareItemById(Long id) {
        return softwareOperations.findById(id).map(this::toParent);
    }

    @Override
    public void addSoftwarePlatform(Long softwareId, Long platformId) {
        try {
            softwareOperations.addPlatform(softwareId, platformId);
        } catch (SoftwareItemNotFoundException e) {
            throw new CollectionNotFoundException(e.getMessage(), e);
        } catch (InvalidSoftwareItemException e) {
            throw new InvalidCollectionException(e.getMessage(), e);
        }
    }

    @Override
    public List<CollectionSoftwarePlatformView> findSoftwarePlatforms(Long softwareId, int limit) {
        try {
            return softwareOperations.findPlatforms(softwareId, limit).stream()
                    .map(this::toParent)
                    .toList();
        } catch (SoftwareItemNotFoundException e) {
            throw new CollectionNotFoundException(e.getMessage(), e);
        } catch (InvalidSoftwareItemException e) {
            throw new InvalidCollectionException(e.getMessage(), e);
        }
    }

    // =========================================================================
    // Mapping Helpers
    // =========================================================================

    private MusicVersion toNested(CollectionMusicVersion version) {
        return version != null ? MusicVersion.valueOf(version.name()) : null;
    }

    private CollectionMusicVersion toParent(MusicVersion version) {
        return version != null ? CollectionMusicVersion.valueOf(version.name()) : null;
    }

    private MusicCreditRole toNested(CollectionMusicCreditRole role) {
        return role != null ? MusicCreditRole.valueOf(role.name()) : null;
    }

    private CollectionMusicCreditRole toParent(MusicCreditRole role) {
        return role != null ? CollectionMusicCreditRole.valueOf(role.name()) : null;
    }

    private ShoppingStatus toNested(CollectionShoppingStatus status) {
        return status != null ? ShoppingStatus.valueOf(status.name()) : null;
    }

    private CollectionShoppingStatus toParent(ShoppingStatus status) {
        return status != null ? CollectionShoppingStatus.valueOf(status.name()) : null;
    }

    private SoftwareType toNested(CollectionSoftwareType type) {
        return type != null ? SoftwareType.valueOf(type.name()) : null;
    }

    private CollectionSoftwareType toParent(SoftwareType type) {
        return type != null ? CollectionSoftwareType.valueOf(type.name()) : null;
    }

    private CollectionMusicView toParent(MusicView view) {
        return new CollectionMusicView(
                view.id(),
                view.title(),
                toParent(view.version()),
                view.platformId(),
                view.url()
        );
    }

    private CollectionMusicCreditView toParent(MusicCreditView view) {
        return new CollectionMusicCreditView(
                view.musicId(),
                view.personId(),
                toParent(view.role())
        );
    }

    private CollectionShoppingItemView toParent(ShoppingItemView view) {
        return new CollectionShoppingItemView(
                view.id(),
                view.name(),
                view.avatarUrl(),
                view.description(),
                view.priceAmount(),
                view.currencyCode(),
                view.platformId(),
                toParent(view.status()),
                view.url(),
                view.purchasedAt()
        );
    }

    private CollectionSoftwareItemView toParent(SoftwareItemView view) {
        return new CollectionSoftwareItemView(
                view.id(),
                view.name(),
                toParent(view.type()),
                view.logoUrl(),
                view.description(),
                view.priceAmount(),
                view.currencyCode(),
                view.url(),
                view.review()
        );
    }

    private CollectionSoftwarePlatformView toParent(SoftwarePlatformView view) {
        return new CollectionSoftwarePlatformView(
                view.softwareId(),
                view.platformId()
        );
    }
}
