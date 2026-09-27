package com.vhvkhangg.personalprivatevault.vault.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.vault.internal.domain.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
}
