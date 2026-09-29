package com.vhvkhangg.personalprivatevault.media.internal.application;

import com.vhvkhangg.personalprivatevault.media.album.AlbumNotFoundException;
import com.vhvkhangg.personalprivatevault.media.album.AlbumOperations;
import com.vhvkhangg.personalprivatevault.media.album.CreateAlbumCommand;
import com.vhvkhangg.personalprivatevault.media.album.InvalidAlbumException;
import com.vhvkhangg.personalprivatevault.media.album.UpdateAlbumCommand;
import com.vhvkhangg.personalprivatevault.media.internal.domain.Album;
import com.vhvkhangg.personalprivatevault.media.internal.infrastructure.persistence.AlbumRepository;
import com.vhvkhangg.personalprivatevault.media.internal.infrastructure.persistence.ImageRepository;
import com.vhvkhangg.personalprivatevault.media.view.AlbumView;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlbumService implements AlbumOperations {

    private final VaultEntryOperations vaultEntryOperations;
    private final AlbumRepository albumRepository;
    private final ImageRepository imageRepository;

    @Override
    @Transactional
    public AlbumView create(CreateAlbumCommand command) {
        if (command == null) {
            throw new InvalidAlbumException("CreateAlbumCommand must not be null");
        }
        validateTitle(command.title());

        VaultEntryView vaultEntry = vaultEntryOperations.create(VaultEntryType.ALBUM);
        Album album = new Album(
                vaultEntry.id(),
                command.title().trim(),
                command.description(),
                true
        );
        albumRepository.save(album);
        return new AlbumView(album.getId(), album.getTitle(), album.getDescription(), 0L);
    }

    @Override
    @Transactional
    public AlbumView update(UpdateAlbumCommand command) {
        if (command == null) {
            throw new InvalidAlbumException("UpdateAlbumCommand must not be null");
        }
        if (command.id() == null) {
            throw new InvalidAlbumException("Album ID must not be null");
        }
        validateTitle(command.title());

        Album album = albumRepository.findById(command.id())
                .orElseThrow(() -> new AlbumNotFoundException(command.id()));

        album.update(command.title().trim(), command.description());
        albumRepository.save(album);
        long imageCount = imageRepository.countByAlbumId(album.getId());
        return new AlbumView(album.getId(), album.getTitle(), album.getDescription(), imageCount);
    }

    @Override
    @Transactional(readOnly = true)
    public AlbumView findById(Long id) {
        if (id == null) {
            throw new AlbumNotFoundException("Album ID must not be null");
        }
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new AlbumNotFoundException(id));
        long imageCount = imageRepository.countByAlbumId(album.getId());
        return new AlbumView(album.getId(), album.getTitle(), album.getDescription(), imageCount);
    }

    @Override
    @Transactional(readOnly = true)
    public long getImageCount(Long albumId) {
        if (albumId == null) {
            throw new AlbumNotFoundException("Album ID must not be null");
        }
        if (!albumRepository.existsById(albumId)) {
            throw new AlbumNotFoundException(albumId);
        }
        return imageRepository.countByAlbumId(albumId);
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new InvalidAlbumException("Album title must not be blank");
        }
        if (title.length() > 500) {
            throw new InvalidAlbumException("Album title must not exceed 500 characters");
        }
    }
}
