/**
 * Owns the RAG (Retrieval-Augmented Generation) foundation, authoritative source selection,
 * monotonic policy epochs, bounded job admission/leases, DB-enforced staging fences,
 * Java-owned guarded finalization, and safe revocation/purge.
 *
 * <h2>Owned persistence</h2>
 * <ul>
 *   <li>{@code rag.sources}</li>
 *   <li>{@code rag.job_leases}</li>
 *   <li>{@code rag.staged_outputs}</li>
 *   <li>{@code rag.dependencies}</li>
 *   <li>{@code rag.index_generations}</li>
 *   <li>{@code rag.runs}</li>
 *   <li>{@code rag.worker_grants}</li>
 *   <li>{@code rag.runtime_state}</li>
 *   <li>{@code rag.external_file_sources}</li>
 *   <li>{@code rag.batch_roots}</li>
 *   <li>{@code rag.file_outcomes}</li>
 *   <li>{@code rag.generation_roots}</li>
 *   <li>{@code rag.document_chunks}</li>
 *   <li>{@code rag.staged_document_outputs}</li>
 * </ul>
 *
 * <h2>Module boundary</h2>
 * <p>Allowed dependencies: {@code vault :: entry}, {@code fiction :: source}, {@code film :: source}.</p>
 * <p>Public contracts belong under {@code api} and {@code source}. Entities, repositories, application
 * services, and web adapters remain under {@code internal}. Other modules must not import this module's
 * {@code internal} packages.</p>
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {
                "vault :: entry",
                "fiction :: source",
                "film :: source"
        }
)
package com.vhvkhangg.personalprivatevault.rag;
