package com.vhvkhangg.personalprivatevault.collection.shopping.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.collection.shopping.internal.domain.ShoppingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link ShoppingItem}.
 */
@Repository
public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, Long> {
}
