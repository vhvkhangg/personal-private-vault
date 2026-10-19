package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import java.util.List;
import java.util.UUID;

public record FileOutcomeDto(
        String rootKey,
        String result,
        UUID candidateGenerationId,
        List<String> warningCodes,
        int chunkCount,
        int pageCount,
        String errorCode,
        String errorMessage,
        String currentIndexState
) {
}
