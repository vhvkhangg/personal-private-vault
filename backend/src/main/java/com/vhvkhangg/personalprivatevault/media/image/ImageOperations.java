package com.vhvkhangg.personalprivatevault.media.image;

import com.vhvkhangg.personalprivatevault.media.view.ImageView;

import java.util.List;

/**
 * Public capability-oriented operations for Image metadata.
 */
public interface ImageOperations {

    /**
     * Creates a new Image metadata record, backed by a Vault Entry of type IMAGE.
     *
     * @param command creation command
     * @return the created image view
     * @throws InvalidImageException if input parameters are invalid
     * @throws ImageConflictException if object_key or checksum_sha256 already exists
     */
    ImageView create(CreateImageCommand command);

    /**
     * Updates metadata of an existing Image.
     *
     * @param command update metadata command
     * @return the updated image view
     * @throws ImageNotFoundException if the image does not exist
     * @throws InvalidImageException if input parameters are invalid
     */
    ImageView updateMetadata(UpdateImageMetadataCommand command);

    /**
     * Finds an Image by its ID.
     *
     * @param id image ID
     * @return the image view
     * @throws ImageNotFoundException if the image does not exist
     */
    ImageView findById(Long id);

    /**
     * Finds images belonging to the specified album with bounded pagination.
     *
     * @param albumId album ID
     * @param limit maximum number of images to return (must be positive, capped at 100)
     * @param offset pagination offset (must be non-negative)
     * @return list of image views
     */
    List<ImageView> findByAlbumId(Long albumId, int limit, int offset);
}
