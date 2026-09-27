package com.vhvkhangg.personalprivatevault.reference.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.reference.internal.domain.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CurrencyRepository extends JpaRepository<Currency, String> {
    List<Currency> findAllByOrderByCodeAsc();
}
