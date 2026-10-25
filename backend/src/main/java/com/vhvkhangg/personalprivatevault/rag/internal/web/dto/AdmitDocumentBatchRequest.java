package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Map;

public record AdmitDocumentBatchRequest(
        @NotEmpty List<@Valid DocumentRootItemDto> roots,
        Map<Long, String> descriptionConfirmations,
        java.util.Set<String> noChangeRoots
) {
    public AdmitDocumentBatchRequest(List<DocumentRootItemDto> roots,Map<Long,String> confirmations) {this(roots,confirmations,java.util.Set.of());}
}
