package com.vhvkhangg.personalprivatevault.location.brand;

import com.vhvkhangg.personalprivatevault.location.view.BrandView;

/**
 * Public capability-oriented operations for Brands.
 */
public interface BrandOperations {

    /**
     * Creates a new Brand, backed by a Vault Entry of type BRAND.
     *
     * @param command creation command
     * @return the created brand view
     * @throws InvalidBrandException if input parameters are invalid
     */
    BrandView create(CreateBrandCommand command);

    /**
     * Updates an existing Brand.
     *
     * @param command update command
     * @return the updated brand view
     * @throws BrandNotFoundException if the brand does not exist
     * @throws InvalidBrandException if input parameters are invalid
     */
    BrandView update(UpdateBrandCommand command);

    /**
     * Finds a Brand by its ID.
     *
     * @param id brand ID
     * @return the brand view
     * @throws BrandNotFoundException if the brand does not exist
     */
    BrandView findById(Long id);
}
