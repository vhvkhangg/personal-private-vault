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
        String currentIndexState,
        String freshnessBasis,
        String snapshotIndexState,
        String originalFreshness,
        boolean publishedNewGeneration,
        UUID selectedRevisionId,
        UUID pendingRevisionId
) {
    public FileOutcomeDto(String key,String result,UUID generation,List<String> warnings,int chunks,int pages,String code,String message,String state) {
        this(key,result,generation,warnings,chunks,pages,code,message,state,null,null,null,false,null,null);
    }
}
