package com.vhvkhangg.personalprivatevault.importdata.job;

import com.vhvkhangg.personalprivatevault.importdata.job.command.CreateImportJobCommand;
import com.vhvkhangg.personalprivatevault.importdata.job.command.ExecuteImportJobCommand;
import com.vhvkhangg.personalprivatevault.importdata.view.ImportJobItemView;
import com.vhvkhangg.personalprivatevault.importdata.view.ImportJobView;

import java.util.List;
import java.util.Optional;

/**
 * Public synchronous API for managing import jobs, staged validation, and whole-job execution.
 */
public interface ImportJobOperations {

    ImportJobView createJob(CreateImportJobCommand command);

    ImportJobView parse(Long jobId, String rawText);

    ImportJobView validate(Long jobId);

    ImportJobView execute(Long jobId, ExecuteImportJobCommand command);

    ImportJobView cancel(Long jobId);

    Optional<ImportJobView> findJobById(Long id);

    default List<ImportJobItemView> findJobItems(Long jobId, int limit) {
        return findJobItems(jobId, 0, limit);
    }

    List<ImportJobItemView> findJobItems(Long jobId, int page, int limit);

    List<ImportJobView> findRecentJobs(int limit);
}
