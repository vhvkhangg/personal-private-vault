package com.vhvkhangg.personalprivatevault.vault.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.vault.internal.domain.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, Long> {
}
