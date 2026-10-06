package com.vhvkhangg.personalprivatevault.importdata.internal.web.mapper;

import com.vhvkhangg.personalprivatevault.importdata.internal.web.dto.CreateImportJobRequest;
import com.vhvkhangg.personalprivatevault.importdata.internal.web.dto.ExecuteImportJobRequest;
import com.vhvkhangg.personalprivatevault.importdata.internal.web.dto.ImportItemDecisionRequest;
import com.vhvkhangg.personalprivatevault.importdata.internal.web.dto.ImportJobItemResponse;
import com.vhvkhangg.personalprivatevault.importdata.internal.web.dto.ImportJobResponse;
import com.vhvkhangg.personalprivatevault.importdata.job.command.CreateImportJobCommand;
import com.vhvkhangg.personalprivatevault.importdata.job.command.ExecuteImportJobCommand;
import com.vhvkhangg.personalprivatevault.importdata.job.command.ImportItemDecisionInput;
import com.vhvkhangg.personalprivatevault.importdata.view.ImportJobItemView;
import com.vhvkhangg.personalprivatevault.importdata.view.ImportJobView;

import java.util.List;

public final class ImportDataWebMapper {

    private ImportDataWebMapper() {}

    public static CreateImportJobCommand toCommand(CreateImportJobRequest request) {
        return new CreateImportJobCommand(
                request.targetType(),
                request.format(),
                request.originalFileName(),
                request.rawFileObjectKey()
        );
    }

    public static ExecuteImportJobCommand toCommand(ExecuteImportJobRequest request) {
        List<ImportItemDecisionInput> decisions = request.itemDecisions() != null
                ? request.itemDecisions().stream().map(ImportDataWebMapper::toDecisionInput).toList()
                : List.of();
        return new ExecuteImportJobCommand(decisions);
    }

    public static ImportItemDecisionInput toDecisionInput(ImportItemDecisionRequest request) {
        return new ImportItemDecisionInput(
                request.itemIndex(),
                request.decision()
        );
    }

    public static ImportJobResponse toResponse(ImportJobView view) {
        return new ImportJobResponse(
                view.id(),
                view.targetType(),
                view.format(),
                view.originalFileName(),
                view.fileHash(),
                view.rawFileObjectKey(),
                view.status(),
                view.totalItems(),
                view.validItems(),
                view.duplicateItems(),
                view.invalidItems(),
                view.importedItems(),
                view.createdAt(),
                view.updatedAt()
        );
    }

    public static ImportJobItemResponse toResponse(ImportJobItemView view) {
        return new ImportJobItemResponse(
                view.id(),
                view.importJobId(),
                view.itemIndex(),
                view.parsedPayload(),
                view.status(),
                view.duplicateVaultEntryId(),
                view.errorMessage(),
                view.importedVaultEntryId()
        );
    }
}
