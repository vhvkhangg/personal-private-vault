package com.vhvkhangg.personalprivatevault.reference.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "countries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Country {

    @Id
    @Column(name = "code", length = 2, nullable = false)
    private String code;

    @Column(name = "name_en", length = 150, nullable = false)
    private String nameEn;

    @Column(name = "name_vi", length = 150)
    private String nameVi;

    public Country(String code, String nameEn, String nameVi) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Country code must not be null or blank");
        }
        if (nameEn == null || nameEn.isBlank()) {
            throw new IllegalArgumentException("Country nameEn must not be null or blank");
        }
        this.code = code;
        this.nameEn = nameEn;
        this.nameVi = nameVi;
    }
}
