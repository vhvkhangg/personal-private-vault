package com.vhvkhangg.personalprivatevault.reference.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "currencies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Currency {

    @Id
    @Column(name = "code", length = 3, nullable = false)
    private String code;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "symbol", length = 16)
    private String symbol;

    @Column(name = "decimal_places", nullable = false)
    private int decimalPlaces;

    public Currency(String code, String name, String symbol, int decimalPlaces) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Currency code must not be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Currency name must not be null or blank");
        }
        this.code = code;
        this.name = name;
        this.symbol = symbol;
        this.decimalPlaces = decimalPlaces;
    }
}
