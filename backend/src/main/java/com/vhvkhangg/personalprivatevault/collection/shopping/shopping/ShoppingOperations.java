package com.vhvkhangg.personalprivatevault.collection.shopping.shopping;

import com.vhvkhangg.personalprivatevault.collection.shopping.view.ShoppingItemView;

import java.util.Optional;

/**
 * Public capability interface for the {@code collection.shopping} nested module.
 */
public interface ShoppingOperations {

    ShoppingItemView create(CreateShoppingItemCommand command);

    ShoppingItemView update(Long id, UpdateShoppingItemCommand command);

    Optional<ShoppingItemView> findById(Long id);
}
