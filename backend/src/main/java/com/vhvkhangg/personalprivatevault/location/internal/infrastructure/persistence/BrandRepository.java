package com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.location.internal.domain.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
}
