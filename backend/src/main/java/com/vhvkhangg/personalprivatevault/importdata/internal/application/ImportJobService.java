package com.vhvkhangg.personalprivatevault.importdata.internal.application;

import com.vhvkhangg.personalprivatevault.importdata.internal.domain.Sha256Util;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportFormat;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportItemDecision;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportItemStatus;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportJobStatus;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportTargetType;
import com.vhvkhangg.personalprivatevault.importdata.internal.domain.ImportJob;
import com.vhvkhangg.personalprivatevault.importdata.internal.domain.ImportJobItem;
import com.vhvkhangg.personalprivatevault.importdata.internal.infrastructure.persistence.ImportJobItemRepository;
import com.vhvkhangg.personalprivatevault.importdata.internal.infrastructure.persistence.ImportJobRepository;
import com.vhvkhangg.personalprivatevault.importdata.internal.parsing.CsvImportParser;
import com.vhvkhangg.personalprivatevault.importdata.internal.parsing.ImportParsedItem;
import com.vhvkhangg.personalprivatevault.importdata.internal.parsing.JsonImportParser;
import com.vhvkhangg.personalprivatevault.importdata.internal.parsing.MarkdownImportParser;
import com.vhvkhangg.personalprivatevault.importdata.job.ImportJobOperations;
import com.vhvkhangg.personalprivatevault.importdata.job.command.CreateImportJobCommand;
import com.vhvkhangg.personalprivatevault.importdata.job.command.ExecuteImportJobCommand;
import com.vhvkhangg.personalprivatevault.importdata.job.command.ImportItemDecisionInput;
import com.vhvkhangg.personalprivatevault.importdata.job.exception.ImportJobNotFoundException;
import com.vhvkhangg.personalprivatevault.importdata.job.exception.InvalidImportJobException;
import com.vhvkhangg.personalprivatevault.importdata.job.exception.InvalidImportTransitionException;
import com.vhvkhangg.personalprivatevault.importdata.view.ImportJobItemView;
import com.vhvkhangg.personalprivatevault.importdata.view.ImportJobView;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeInformationItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeNoteCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeStudyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeVocabularyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeInformationItemView;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeInformationType;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeNoteView;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeOperations;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeStudyItemView;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeStudyStatus;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeStudyType;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeVocabularyItemView;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeVocabularyLearningStatus;
import com.vhvkhangg.personalprivatevault.knowledge.api.UpdateKnowledgeInformationItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.UpdateKnowledgeNoteCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.UpdateKnowledgeStudyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.UpdateKnowledgeVocabularyItemCommand;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service implementing import job lifecycle, parsing, validation, and whole-job execution.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImportJobService implements ImportJobOperations {

    private final ImportJobRepository importJobRepository;
    private final ImportJobItemRepository importJobItemRepository;
    private final KnowledgeOperations knowledgeOperations;
    private final VaultEntryOperations vaultEntryOperations;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public ImportJobView createJob(CreateImportJobCommand command) {
        if (command == null) {
            throw new InvalidImportJobException("CreateImportJobCommand must not be null");
        }
        if (command.targetType() == null) {
            throw new InvalidImportJobException("Target type must not be null");
        }
        if (command.format() == null) {
            throw new InvalidImportJobException("Format must not be null");
        }
        if (command.format() == ImportFormat.MARKDOWN && command.targetType() != ImportTargetType.NOTE) {
            throw new InvalidImportJobException("Markdown format is only supported for NOTE target");
        }
        if (command.originalFileName() == null || command.originalFileName().isBlank()) {
            throw new InvalidImportJobException("Original file name must not be blank");
        }
        if (command.originalFileName().trim().length() > 500) {
            throw new InvalidImportJobException("Original file name must not exceed 500 characters");
        }
        if (command.rawFileObjectKey() != null && command.rawFileObjectKey().trim().length() > 1024) {
            throw new InvalidImportJobException("Raw file object key must not exceed 1024 characters");
        }

        ImportJob job = new ImportJob(
                command.targetType(),
                command.format(),
                command.originalFileName().trim(),
                command.rawFileObjectKey() != null ? command.rawFileObjectKey().trim() : null
        );

        ImportJob saved = importJobRepository.save(job);
        return toView(saved);
    }

    @Override
    @Transactional
    public ImportJobView parse(Long jobId, String rawText) {
        ImportJob job = acquireGuardAndRefresh(jobId);
        if (job.getStatus() != ImportJobStatus.CREATED) {
            throw new InvalidImportTransitionException(jobId, job.getStatus(), "PARSE");
        }
        if (rawText == null || rawText.isBlank()) {
            throw new InvalidImportJobException("Import raw text must not be blank");
        }

        String fileHash = Sha256Util.computeSha256(rawText);
        importJobItemRepository.deleteByImportJobId(jobId);

        List<ImportParsedItem> parsedItems;
        try {
            parsedItems = switch (job.getFormat()) {
                case CSV -> CsvImportParser.parse(job.getTargetType(), rawText);
                case JSON -> JsonImportParser.parse(job.getTargetType(), rawText);
                case MARKDOWN -> MarkdownImportParser.parse(job.getTargetType(), job.getOriginalFileName(), fileHash, rawText);
            };
        } catch (Exception ex) {
            log.warn("Catastrophic parse failure for import job {}", jobId);
            job.transitionToFailed();
            importJobRepository.save(job);
            if (ex instanceof InvalidImportJobException) {
                throw (InvalidImportJobException) ex;
            }
            throw new InvalidImportJobException("Failed to parse import content due to syntax or format error");
        }

        int total = parsedItems.size();
        int valid = 0;
        int invalid = 0;

        List<ImportJobItem> itemEntities = new ArrayList<>();
        for (ImportParsedItem item : parsedItems) {
            if (item.status() == ImportItemStatus.VALID) {
                valid++;
            } else {
                invalid++;
            }
            itemEntities.add(new ImportJobItem(
                    job.getId(),
                    item.itemIndex(),
                    item.payload(),
                    item.status(),
                    item.errorMessage()
            ));
        }

        importJobItemRepository.saveAll(itemEntities);
        job.transitionToParsed(fileHash, total, valid, invalid);
        ImportJob saved = importJobRepository.save(job);
        return toView(saved);
    }

    @Override
    @Transactional
    public ImportJobView validate(Long jobId) {
        ImportJob job = acquireGuardAndRefresh(jobId);
        if (job.getStatus() != ImportJobStatus.PARSED) {
            throw new InvalidImportTransitionException(jobId, job.getStatus(), "VALIDATE");
        }

        List<ImportJobItem> items = importJobItemRepository.findByImportJobIdOrderByItemIndexAsc(jobId);
        int valid = 0;
        int duplicate = 0;
        int invalid = 0;

        for (ImportJobItem item : items) {
            if (item.getStatus() == ImportItemStatus.INVALID) {
                invalid++;
                continue;
            }

            if (job.getTargetType() == ImportTargetType.NOTE) {
                Map<String, Object> payload = item.getParsedPayload();
                String noteHash = payload != null ? (String) payload.get("importedFileHash") : null;
                if (noteHash != null && !noteHash.isBlank()) {
                    Optional<KnowledgeNoteView> existing = knowledgeOperations.findNoteByImportedFileHash(noteHash);
                    if (existing.isPresent()) {
                        item.markDuplicate(existing.get().id());
                        importJobItemRepository.save(item);
                        duplicate++;
                        continue;
                    }
                }
            }

            // Other targets or non-duplicate notes remain VALID
            valid++;
        }

        job.transitionToValidated(valid, duplicate, invalid);
        ImportJob saved = importJobRepository.save(job);
        return toView(saved);
    }

    @Override
    @Transactional
    public ImportJobView execute(Long jobId, ExecuteImportJobCommand command) {
        ImportJob job = acquireGuardAndRefresh(jobId);
        if (job.getStatus() != ImportJobStatus.VALIDATED) {
            throw new InvalidImportTransitionException(jobId, job.getStatus(), "EXECUTE");
        }
        if (command == null || command.itemDecisions() == null) {
            throw new InvalidImportJobException("ExecuteImportJobCommand and itemDecisions must not be null");
        }

        List<ImportJobItem> items = importJobItemRepository.findByImportJobIdOrderByItemIndexAsc(jobId);
        if (items.size() != command.itemDecisions().size()) {
            throw new InvalidImportJobException("Decisions count (" + command.itemDecisions().size() +
                    ") does not match job total items (" + items.size() + ")");
        }

        Map<Integer, ImportItemDecision> decisionsByIndex = new LinkedHashMap<>();
        for (ImportItemDecisionInput decisionInput : command.itemDecisions()) {
            if (decisionInput == null || decisionInput.decision() == null) {
                throw new InvalidImportJobException("Decision input and decision value must not be null");
            }
            if (decisionsByIndex.put(decisionInput.itemIndex(), decisionInput.decision()) != null) {
                throw new InvalidImportJobException("Duplicate decision for itemIndex: " + decisionInput.itemIndex());
            }
        }

        for (ImportJobItem item : items) {
            ImportItemDecision decision = decisionsByIndex.get(item.getItemIndex());
            if (decision == null) {
                throw new InvalidImportJobException("Missing decision for itemIndex: " + item.getItemIndex());
            }

            if (item.getStatus() == ImportItemStatus.VALID) {
                if (decision == ImportItemDecision.UPDATE) {
                    throw new InvalidImportJobException("Item at index " + item.getItemIndex() + " is VALID and cannot be updated");
                }
            } else if (item.getStatus() == ImportItemStatus.DUPLICATE) {
                if (decision == ImportItemDecision.IMPORT) {
                    throw new InvalidImportJobException("Item at index " + item.getItemIndex() + " is DUPLICATE and cannot be imported as new");
                }
            } else if (item.getStatus() == ImportItemStatus.INVALID) {
                if (decision != ImportItemDecision.SKIP) {
                    throw new InvalidImportJobException("Item at index " + item.getItemIndex() + " is INVALID and must be skipped");
                }
            }
        }

        int importedCount = 0;
        for (ImportJobItem item : items) {
            ImportItemDecision decision = decisionsByIndex.get(item.getItemIndex());
            if (decision == ImportItemDecision.SKIP) {
                item.markSkipped();
                importJobItemRepository.save(item);
                continue;
            }

            if (decision == ImportItemDecision.IMPORT) {
                Long createdId = switch (job.getTargetType()) {
                    case STUDY -> knowledgeOperations.createStudyItem(buildCreateStudyCommand(item.getParsedPayload())).id();
                    case INFORMATION -> knowledgeOperations.createInformationItem(buildCreateInfoCommand(item.getParsedPayload())).id();
                    case VOCABULARY -> knowledgeOperations.createVocabularyItem(buildCreateVocabCommand(item.getParsedPayload())).id();
                    case NOTE -> knowledgeOperations.createNote(buildCreateNoteCommand(item.getParsedPayload())).id();
                };
                item.markImported(createdId);
                importJobItemRepository.save(item);
                importedCount++;
            } else if (decision == ImportItemDecision.UPDATE) {
                Long dupId = item.getDuplicateVaultEntryId();
                if (dupId == null) {
                    throw new InvalidImportJobException("Item marked for update has no duplicate vault entry id");
                }
                VaultEntryView vaultEntry = vaultEntryOperations.find(dupId)
                        .orElseThrow(() -> new InvalidImportJobException("Duplicate vault entry not found with id: " + dupId));

                VaultEntryType expectedVaultType = expectedVaultType(job.getTargetType());
                if (vaultEntry.entryType() != expectedVaultType) {
                    throw new InvalidImportJobException("Vault entry type " + vaultEntry.entryType() +
                            " does not match expected target type " + expectedVaultType);
                }

                switch (job.getTargetType()) {
                    case STUDY -> knowledgeOperations.updateStudyItem(dupId, buildUpdateStudyCommand(item.getParsedPayload()));
                    case INFORMATION -> knowledgeOperations.updateInformationItem(dupId, buildUpdateInfoCommand(item.getParsedPayload()));
                    case VOCABULARY -> knowledgeOperations.updateVocabularyItem(dupId, buildUpdateVocabCommand(item.getParsedPayload()));
                    case NOTE -> knowledgeOperations.updateNote(dupId, buildUpdateNoteCommand(item.getParsedPayload()));
                }
                item.markUpdated(dupId);
                importJobItemRepository.save(item);
                importedCount++;
            }
        }

        job.transitionToImported(importedCount);
        ImportJob saved = importJobRepository.save(job);
        return toView(saved);
    }

    @Override
    @Transactional
    public ImportJobView cancel(Long jobId) {
        ImportJob job = acquireGuardAndRefresh(jobId);
        if (job.getStatus() == ImportJobStatus.CREATED ||
                job.getStatus() == ImportJobStatus.PARSED ||
                job.getStatus() == ImportJobStatus.VALIDATED) {
            job.transitionToCancelled();
            ImportJob saved = importJobRepository.save(job);
            return toView(saved);
        }

        throw new InvalidImportTransitionException(jobId, job.getStatus(), "CANCEL");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ImportJobView> findJobById(Long id) {
        return importJobRepository.findById(id).map(this::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImportJobItemView> findJobItems(Long jobId, int limit) {
        if (limit <= 0) {
            throw new InvalidImportJobException("Limit must be positive");
        }
        return importJobItemRepository.findByImportJobIdOrderByItemIndexAsc(jobId, PageRequest.of(0, limit))
                .stream()
                .map(this::toItemView)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImportJobView> findRecentJobs(int limit) {
        if (limit <= 0) {
            throw new InvalidImportJobException("Limit must be positive");
        }
        return importJobRepository.findRecent(PageRequest.of(0, limit))
                .stream()
                .map(this::toView)
                .toList();
    }

    private ImportJob acquireGuardAndRefresh(Long jobId) {
        ImportJob job = importJobRepository.findByIdForUpdate(jobId)
                .orElseThrow(() -> new ImportJobNotFoundException(jobId));
        entityManager.refresh(job, LockModeType.PESSIMISTIC_WRITE);
        return job;
    }

    private VaultEntryType expectedVaultType(ImportTargetType targetType) {
        return switch (targetType) {
            case STUDY -> VaultEntryType.STUDY;
            case INFORMATION -> VaultEntryType.INFORMATION;
            case VOCABULARY -> VaultEntryType.VOCABULARY;
            case NOTE -> VaultEntryType.NOTE;
        };
    }

    private CreateKnowledgeStudyItemCommand buildCreateStudyCommand(Map<String, Object> p) {
        return new CreateKnowledgeStudyItemCommand(
                (String) p.get("title"),
                (String) p.get("posterUrl"),
                p.get("type") != null ? KnowledgeStudyType.valueOf((String) p.get("type")) : null,
                (String) p.get("siteDomain"),
                toLong(p.get("youtubeChannelAccountId")),
                toLong(p.get("authorPersonId")),
                toLong(p.get("authorGroupId")),
                toLocalDate(p.get("publishedDate")),
                toBigDecimal(p.get("priceAmount")),
                (String) p.get("currencyCode"),
                (String) p.get("description"),
                (String) p.get("url"),
                (String) p.get("review"),
                p.get("learningStatus") != null ? KnowledgeStudyStatus.valueOf((String) p.get("learningStatus")) : null,
                toBigDecimal(p.get("progressPercent")),
                (String) p.get("currentProgressText")
        );
    }

    private UpdateKnowledgeStudyItemCommand buildUpdateStudyCommand(Map<String, Object> p) {
        return new UpdateKnowledgeStudyItemCommand(
                (String) p.get("title"),
                (String) p.get("posterUrl"),
                p.get("type") != null ? KnowledgeStudyType.valueOf((String) p.get("type")) : null,
                (String) p.get("siteDomain"),
                toLong(p.get("youtubeChannelAccountId")),
                toLong(p.get("authorPersonId")),
                toLong(p.get("authorGroupId")),
                toLocalDate(p.get("publishedDate")),
                toBigDecimal(p.get("priceAmount")),
                (String) p.get("currencyCode"),
                (String) p.get("description"),
                (String) p.get("url"),
                (String) p.get("review"),
                p.get("learningStatus") != null ? KnowledgeStudyStatus.valueOf((String) p.get("learningStatus")) : null,
                toBigDecimal(p.get("progressPercent")),
                (String) p.get("currentProgressText")
        );
    }

    private CreateKnowledgeInformationItemCommand buildCreateInfoCommand(Map<String, Object> p) {
        return new CreateKnowledgeInformationItemCommand(
                (String) p.get("title"),
                p.get("type") != null ? KnowledgeInformationType.valueOf((String) p.get("type")) : null,
                (String) p.get("description"),
                (String) p.get("contentMarkdown"),
                (String) p.get("example"),
                (String) p.get("sourceName"),
                (String) p.get("sourceUrl")
        );
    }

    private UpdateKnowledgeInformationItemCommand buildUpdateInfoCommand(Map<String, Object> p) {
        return new UpdateKnowledgeInformationItemCommand(
                (String) p.get("title"),
                p.get("type") != null ? KnowledgeInformationType.valueOf((String) p.get("type")) : null,
                (String) p.get("description"),
                (String) p.get("contentMarkdown"),
                (String) p.get("example"),
                (String) p.get("sourceName"),
                (String) p.get("sourceUrl")
        );
    }

    private CreateKnowledgeVocabularyItemCommand buildCreateVocabCommand(Map<String, Object> p) {
        return new CreateKnowledgeVocabularyItemCommand(
                (String) p.get("word"),
                (String) p.get("languageCode"),
                (String) p.get("meaning"),
                (String) p.get("example"),
                (String) p.get("pronunciation"),
                (String) p.get("ipa"),
                (String) p.get("partOfSpeech"),
                (String) p.get("sourceName"),
                (String) p.get("sourceUrl"),
                p.get("learningStatus") != null ? KnowledgeVocabularyLearningStatus.valueOf((String) p.get("learningStatus")) : null,
                toInstant(p.get("nextReviewAt")),
                toInteger(p.get("intervalDays")),
                toBigDecimal(p.get("easeFactor")),
                toInteger(p.get("repetitionCount")),
                toInteger(p.get("lapseCount"))
        );
    }

    private UpdateKnowledgeVocabularyItemCommand buildUpdateVocabCommand(Map<String, Object> p) {
        return new UpdateKnowledgeVocabularyItemCommand(
                (String) p.get("word"),
                (String) p.get("languageCode"),
                (String) p.get("meaning"),
                (String) p.get("example"),
                (String) p.get("pronunciation"),
                (String) p.get("ipa"),
                (String) p.get("partOfSpeech"),
                (String) p.get("sourceName"),
                (String) p.get("sourceUrl"),
                p.get("learningStatus") != null ? KnowledgeVocabularyLearningStatus.valueOf((String) p.get("learningStatus")) : null,
                toInstant(p.get("nextReviewAt")),
                toInteger(p.get("intervalDays")),
                toBigDecimal(p.get("easeFactor")),
                toInteger(p.get("repetitionCount")),
                toInteger(p.get("lapseCount"))
        );
    }

    @SuppressWarnings("unchecked")
    private CreateKnowledgeNoteCommand buildCreateNoteCommand(Map<String, Object> p) {
        Map<String, Object> frontmatter = (Map<String, Object>) p.get("frontmatter");
        return new CreateKnowledgeNoteCommand(
                (String) p.get("title"),
                (String) p.get("contentMarkdown"),
                (String) p.get("summary"),
                (String) p.get("sourceName"),
                (String) p.get("sourceUrl"),
                (String) p.get("importedFileName"),
                (String) p.get("importedFileHash"),
                frontmatter != null ? frontmatter : Collections.emptyMap()
        );
    }

    @SuppressWarnings("unchecked")
    private UpdateKnowledgeNoteCommand buildUpdateNoteCommand(Map<String, Object> p) {
        Map<String, Object> frontmatter = (Map<String, Object>) p.get("frontmatter");
        return new UpdateKnowledgeNoteCommand(
                (String) p.get("title"),
                (String) p.get("contentMarkdown"),
                (String) p.get("summary"),
                (String) p.get("sourceName"),
                (String) p.get("sourceUrl"),
                (String) p.get("importedFileName"),
                (String) p.get("importedFileHash"),
                frontmatter != null ? frontmatter : Collections.emptyMap()
        );
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number n) return n.longValue();
        return Long.parseLong(val.toString());
    }

    private Integer toInteger(Object val) {
        if (val == null) return null;
        if (val instanceof Number n) return n.intValue();
        return Integer.parseInt(val.toString());
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return null;
        if (val instanceof BigDecimal bd) return bd;
        if (val instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(val.toString());
    }

    private LocalDate toLocalDate(Object val) {
        if (val == null) return null;
        if (val instanceof LocalDate ld) return ld;
        return LocalDate.parse(val.toString());
    }

    private Instant toInstant(Object val) {
        if (val == null) return null;
        if (val instanceof Instant i) return i;
        return Instant.parse(val.toString());
    }

    private ImportJobView toView(ImportJob j) {
        return new ImportJobView(
                j.getId(),
                j.getTargetType(),
                j.getFormat(),
                j.getOriginalFileName(),
                j.getFileHash(),
                j.getRawFileObjectKey(),
                j.getStatus(),
                j.getTotalItems(),
                j.getValidItems(),
                j.getDuplicateItems(),
                j.getInvalidItems(),
                j.getImportedItems(),
                j.getCreatedAt(),
                j.getUpdatedAt()
        );
    }

    private ImportJobItemView toItemView(ImportJobItem i) {
        return new ImportJobItemView(
                i.getId(),
                i.getImportJobId(),
                i.getItemIndex(),
                i.getParsedPayload(),
                i.getStatus(),
                i.getDuplicateVaultEntryId(),
                i.getErrorMessage(),
                i.getImportedVaultEntryId()
        );
    }
}
