package com.vhvkhangg.personalprivatevault.media.album;

import com.vhvkhangg.personalprivatevault.media.view.AlbumView;

/**
 * Public capability-oriented operations for Albums.
 */
public interface AlbumOperations {

    /**
     * Creates a new Album, backed by a Vault Entry of type ALBUM.
     *
     * @param command creation command
     * @return the created album view
     * @throws InvalidAlbumException if input parameters are invalid
     */
    AlbumView create(CreateAlbumCommand command);

    /**
     * Updates an existing Album.
     *
     * @param command update command
     * @return the updated album view
     * @throws AlbumNotFoundException if the album does not exist
     * @throws InvalidAlbumException if input parameters are invalid
     */
    AlbumView update(UpdateAlbumCommand command);

    /**
     * Finds an Album by its ID.
     *
     * @param id album ID
     * @return the album view
     * @throws AlbumNotFoundException if the album does not exist
     */
    AlbumView findById(Long id);

    /**
     * Obtains the derived count of images belonging to the specified album.
     *
     * @param albumId album ID
     * @return total count of images
     * @throws AlbumNotFoundException if the album does not exist
     */
    long getImageCount(Long albumId);
}
