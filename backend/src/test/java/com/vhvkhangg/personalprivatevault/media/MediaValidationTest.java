package com.vhvkhangg.personalprivatevault.media;

import com.vhvkhangg.personalprivatevault.media.album.AlbumNotFoundException;
import com.vhvkhangg.personalprivatevault.media.album.CreateAlbumCommand;
import com.vhvkhangg.personalprivatevault.media.album.InvalidAlbumException;
import com.vhvkhangg.personalprivatevault.media.album.UpdateAlbumCommand;
import com.vhvkhangg.personalprivatevault.media.image.CreateImageCommand;
import com.vhvkhangg.personalprivatevault.media.image.ImageConflictException;
import com.vhvkhangg.personalprivatevault.media.image.ImageNotFoundException;
import com.vhvkhangg.personalprivatevault.media.image.InvalidImageException;
import com.vhvkhangg.personalprivatevault.media.image.UpdateImageMetadataCommand;
import com.vhvkhangg.personalprivatevault.media.internal.application.AlbumService;
import com.vhvkhangg.personalprivatevault.media.internal.application.ImageService;
import com.vhvkhangg.personalprivatevault.media.internal.domain.Image;
import com.vhvkhangg.personalprivatevault.media.internal.infrastructure.persistence.AlbumRepository;
import com.vhvkhangg.personalprivatevault.media.internal.infrastructure.persistence.ImageRepository;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MediaValidationTest {

    private AlbumRepository albumRepository;
    private ImageRepository imageRepository;
    private VaultEntryOperations vaultEntryOperations;

    private AlbumService albumService;
    private ImageService imageService;

    @BeforeEach
    void setUp() {
        albumRepository = mock(AlbumRepository.class);
        imageRepository = mock(ImageRepository.class);
        vaultEntryOperations = mock(VaultEntryOperations.class);

        when(vaultEntryOperations.create(any()))
                .thenReturn(new VaultEntryView(1L, VaultEntryType.ALBUM, null, null, null));

        albumService = new AlbumService(vaultEntryOperations, albumRepository, imageRepository);
        imageService = new ImageService(vaultEntryOperations, albumRepository, imageRepository);
    }

    @Nested
    @DisplayName("Album validation")
    class AlbumValidation {

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("Blank title throws InvalidAlbumException")
        void blankTitleThrows(String title) {
            assertThatThrownBy(() -> albumService.create(new CreateAlbumCommand(title, null)))
                    .isInstanceOf(InvalidAlbumException.class)
                    .hasMessageContaining("Album title must not be blank");
        }

        @Test
        @DisplayName("Title exceeding 500 chars throws InvalidAlbumException")
        void titleTooLongThrows() {
            String longTitle = "a".repeat(501);
            assertThatThrownBy(() -> albumService.create(new CreateAlbumCommand(longTitle, null)))
                    .isInstanceOf(InvalidAlbumException.class)
                    .hasMessageContaining("Album title must not exceed 500 characters");
        }

        @Test
        @DisplayName("Update with null id throws InvalidAlbumException")
        void updateNullIdThrows() {
            assertThatThrownBy(() -> albumService.update(new UpdateAlbumCommand(null, "Title", null)))
                    .isInstanceOf(InvalidAlbumException.class)
                    .hasMessageContaining("Album ID must not be null");
        }

        @Test
        @DisplayName("Update non-existent album throws AlbumNotFoundException")
        void updateNotFoundThrows() {
            when(albumRepository.findById(999L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> albumService.update(new UpdateAlbumCommand(999L, "Title", null)))
                    .isInstanceOf(AlbumNotFoundException.class)
                    .hasMessageContaining("Album with ID 999 was not found");
        }

        @Test
        @DisplayName("Find by null id throws AlbumNotFoundException")
        void findNullIdThrows() {
            assertThatThrownBy(() -> albumService.findById(null))
                    .isInstanceOf(AlbumNotFoundException.class)
                    .hasMessageContaining("Album ID must not be null");
        }

        @Test
        @DisplayName("Find non-existent album throws AlbumNotFoundException")
        void findNotFoundThrows() {
            when(albumRepository.findById(999L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> albumService.findById(999L))
                    .isInstanceOf(AlbumNotFoundException.class)
                    .hasMessageContaining("Album with ID 999 was not found");
        }
    }

    @Nested
    @DisplayName("Image validation")
    class ImageValidation {

        private final String validSha = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("Blank object key throws InvalidImageException")
        void blankObjectKeyThrows(String key) {
            CreateImageCommand cmd = new CreateImageCommand(
                    null, "Title", "photo", key, null, "image/jpeg", 1024L, 800, 600, validSha, null, null
            );
            assertThatThrownBy(() -> imageService.create(cmd))
                    .isInstanceOf(InvalidImageException.class)
                    .hasMessageContaining("Image object_key must not be blank");
        }

        @Test
        @DisplayName("Object key exceeding 1024 chars throws InvalidImageException")
        void objectKeyTooLongThrows() {
            String longKey = "photos/" + "k".repeat(1020);
            CreateImageCommand cmd = new CreateImageCommand(
                    null, "Title", "photo", longKey, null, "image/jpeg", 1024L, 800, 600, validSha, null, null
            );
            assertThatThrownBy(() -> imageService.create(cmd))
                    .isInstanceOf(InvalidImageException.class)
                    .hasMessageContaining("Image object_key must not exceed 1024 characters");
        }

        @Test
        @DisplayName("Checksum exceeding 64 chars throws InvalidImageException")
        void checksumTooLongThrows() {
            String longSha = "a".repeat(65);
            CreateImageCommand cmd = new CreateImageCommand(
                    null, "Title", "photo", "key.jpg", null, "image/jpeg", 1024L, 800, 600, longSha, null, null
            );
            assertThatThrownBy(() -> imageService.create(cmd))
                    .isInstanceOf(InvalidImageException.class)
                    .hasMessageContaining("Image checksum_sha256 must not exceed 64 characters");
        }

        @ParameterizedTest
        @ValueSource(longs = {-1L, -100L})
        @DisplayName("Negative byte size throws InvalidImageException")
        void negativeByteSizeThrows(long size) {
            CreateImageCommand cmd = new CreateImageCommand(
                    null, "Title", "photo", "key.jpg", null, "image/jpeg", size, 800, 600, validSha, null, null
            );
            assertThatThrownBy(() -> imageService.create(cmd))
                    .isInstanceOf(InvalidImageException.class)
                    .hasMessageContaining("Image size_bytes must be non-negative");
        }

        @Test
        @DisplayName("Non-positive width throws InvalidImageException")
        void nonPositiveWidthThrows() {
            CreateImageCommand cmd = new CreateImageCommand(
                    null, "Title", "photo", "key.jpg", null, "image/jpeg", 1024L, 0, 600, validSha, null, null
            );
            assertThatThrownBy(() -> imageService.create(cmd))
                    .isInstanceOf(InvalidImageException.class)
                    .hasMessageContaining("Image width_px must be positive");
        }

        @Test
        @DisplayName("Non-positive height throws InvalidImageException")
        void nonPositiveHeightThrows() {
            CreateImageCommand cmd = new CreateImageCommand(
                    null, "Title", "photo", "key.jpg", null, "image/jpeg", 1024L, 800, -5, validSha, null, null
            );
            assertThatThrownBy(() -> imageService.create(cmd))
                    .isInstanceOf(InvalidImageException.class)
                    .hasMessageContaining("Image height_px must be positive");
        }

        @Test
        @DisplayName("Location text exceeding 500 chars throws InvalidImageException")
        void locationTextTooLongThrows() {
            String longLoc = "loc/".repeat(150);
            CreateImageCommand cmd = new CreateImageCommand(
                    null, "Title", "photo", "key.jpg", null, "image/jpeg", 1024L, 800, 600, validSha, null, longLoc
            );
            assertThatThrownBy(() -> imageService.create(cmd))
                    .isInstanceOf(InvalidImageException.class)
                    .hasMessageContaining("Image location_text must not exceed 500 characters");
        }

        @Test
        @DisplayName("Non-existent albumId throws AlbumNotFoundException")
        void nonExistentAlbumThrows() {
            when(albumRepository.existsById(50L)).thenReturn(false);
            CreateImageCommand cmd = new CreateImageCommand(
                    50L, "Title", "photo", "key.jpg", null, "image/jpeg", 1024L, 800, 600, validSha, null, null
            );
            assertThatThrownBy(() -> imageService.create(cmd))
                    .isInstanceOf(AlbumNotFoundException.class);
        }

        @Test
        @DisplayName("Existing objectKey throws ImageConflictException")
        void duplicateObjectKeyThrows() {
            when(imageRepository.findByObjectKey("key.jpg")).thenReturn(Optional.of(mock(Image.class)));
            CreateImageCommand cmd = new CreateImageCommand(
                    null, "Title", "photo", "key.jpg", null, "image/jpeg", 1024L, 800, 600, validSha, null, null
            );
            assertThatThrownBy(() -> imageService.create(cmd))
                    .isInstanceOf(ImageConflictException.class)
                    .hasMessageContaining("Image with object key 'key.jpg' already exists");
        }

        @Test
        @DisplayName("Existing checksum throws ImageConflictException")
        void duplicateChecksumThrows() {
            when(imageRepository.findByObjectKey("key2.jpg")).thenReturn(Optional.empty());
            when(imageRepository.findByChecksumSha256(validSha)).thenReturn(Optional.of(mock(Image.class)));
            CreateImageCommand cmd = new CreateImageCommand(
                    null, "Title", "photo", "key2.jpg", null, "image/jpeg", 1024L, 800, 600, validSha, null, null
            );
            assertThatThrownBy(() -> imageService.create(cmd))
                    .isInstanceOf(ImageConflictException.class)
                    .hasMessageContaining("Image with checksum '" + validSha + "' already exists");
        }

        @Test
        @DisplayName("Persistence conflict on object_key throws domain ImageConflictException")
        void persistenceConflictOnObjectKeyThrows() {
            when(imageRepository.findByObjectKey("race.jpg")).thenReturn(Optional.empty());
            when(imageRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate key violates unique constraint images_object_key_key"));

            CreateImageCommand cmd = new CreateImageCommand(
                    null, "Title", "photo", "race.jpg", null, "image/jpeg", 1024L, 800, 600, null, null, null
            );
            assertThatThrownBy(() -> imageService.create(cmd))
                    .isInstanceOf(ImageConflictException.class)
                    .hasMessage("Image with object key 'race.jpg' already exists");
        }

        @Test
        @DisplayName("Persistence conflict on checksum throws domain ImageConflictException")
        void persistenceConflictOnChecksumThrows() {
            when(imageRepository.findByObjectKey("race2.jpg")).thenReturn(Optional.empty());
            when(imageRepository.findByChecksumSha256(validSha)).thenReturn(Optional.empty());
            when(imageRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate key violates unique constraint images_checksum_sha256_key"));

            CreateImageCommand cmd = new CreateImageCommand(
                    null, "Title", "photo", "race2.jpg", null, "image/jpeg", 1024L, 800, 600, validSha, null, null
            );
            assertThatThrownBy(() -> imageService.create(cmd))
                    .isInstanceOf(ImageConflictException.class)
                    .hasMessage("Image with checksum '" + validSha + "' already exists");
        }

        @Test
        @DisplayName("Unexpected DataIntegrityViolationException uses non-sensitive generic fallback message")
        void unexpectedDataIntegrityViolationUsesNonSensitiveFallback() {
            when(imageRepository.findByObjectKey("fallback.jpg")).thenReturn(Optional.empty());
            when(imageRepository.saveAndFlush(any())).thenThrow(
                    new DataIntegrityViolationException("could not execute statement [ERROR: foreign key violation on table images]; SQL [insert into images ... private_leak]")
            );

            CreateImageCommand cmd = new CreateImageCommand(
                    null, "Title", "photo", "fallback.jpg", null, "image/jpeg", 1024L, 800, 600, null, null, null
            );
            assertThatThrownBy(() -> imageService.create(cmd))
                    .isInstanceOf(ImageConflictException.class)
                    .hasMessage("Image metadata conflict occurred during creation")
                    .satisfies(ex -> {
                        org.assertj.core.api.Assertions.assertThat(ex.getMessage())
                                .doesNotContain("could not execute statement")
                                .doesNotContain("insert into images")
                                .doesNotContain("private_leak");
                    });
        }

        @Test
        @DisplayName("Update non-existent image throws ImageNotFoundException")
        void updateNotFoundThrows() {
            when(imageRepository.findById(999L)).thenReturn(Optional.empty());
            UpdateImageMetadataCommand cmd = new UpdateImageMetadataCommand(
                    999L, null, "Title", "photo", null, "image/jpeg", 1024L, 800, 600, null, null
            );
            assertThatThrownBy(() -> imageService.updateMetadata(cmd))
                    .isInstanceOf(ImageNotFoundException.class)
                    .hasMessageContaining("Image with ID 999 was not found");
        }
    }
}
