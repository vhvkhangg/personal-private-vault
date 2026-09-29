package com.vhvkhangg.personalprivatevault.location.address;

import com.vhvkhangg.personalprivatevault.location.view.AddressView;

/**
 * Public capability-oriented operations for physical Addresses.
 */
public interface AddressOperations {

    /**
     * Creates a new Address.
     *
     * @param command creation command
     * @return the created address view
     * @throws InvalidAddressException if input parameters are invalid
     */
    AddressView create(CreateAddressCommand command);

    /**
     * Updates an existing Address.
     *
     * @param command update command
     * @return the updated address view
     * @throws AddressNotFoundException if the address does not exist
     * @throws InvalidAddressException if input parameters are invalid
     */
    AddressView update(UpdateAddressCommand command);

    /**
     * Finds an Address by its ID.
     *
     * @param id address ID
     * @return the address view
     * @throws AddressNotFoundException if the address does not exist
     */
    AddressView findById(Long id);
}
