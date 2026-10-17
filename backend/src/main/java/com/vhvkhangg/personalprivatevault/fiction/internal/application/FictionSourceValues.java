package com.vhvkhangg.personalprivatevault.fiction.internal.application;

import com.vhvkhangg.personalprivatevault.fiction.enums.*;

/** Fresh owner-local scalar query result; never a managed persistence entity. */
public record FictionSourceValues(Long id,String title,String originalTitle,FictionFormat format,String nationalityCode,
        Long genreId, ProgressStatus progressStatus,ConsumptionStatus consumptionStatus,
        Integer totalChapters,String description,boolean isNsfw) {}
