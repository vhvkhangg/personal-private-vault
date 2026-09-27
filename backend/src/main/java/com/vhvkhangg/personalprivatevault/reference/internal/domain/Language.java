package com.vhvkhangg.personalprivatevault.reference.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "languages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Language {

    @Id
    @Column(name = "code", length = 10, nullable = false)
    private String code;

    @Column(name = "name_en", length = 100, nullable = false)
    private String nameEn;

    @Column(name = "name_vi", length = 100)
    private String nameVi;

    public Language(String code, String nameEn, String nameVi) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Language code must not be null or blank");
        }
        if (nameEn == null || nameEn.isBlank()) {
            throw new IllegalArgumentException("Language nameEn must not be null or blank");
        }
        this.code = code;
        this.nameEn = nameEn;
        this.nameVi = nameVi;
    }
}
