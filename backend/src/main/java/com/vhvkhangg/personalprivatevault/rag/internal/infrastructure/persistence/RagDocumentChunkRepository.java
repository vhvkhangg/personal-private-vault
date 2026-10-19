package com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagDocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RagDocumentChunkRepository extends JpaRepository<RagDocumentChunk, Long> {

    List<RagDocumentChunk> findByGenerationId(Long generationId);

    long countByGenerationId(Long generationId);

    @org.springframework.data.jpa.repository.Query(
            value = "SELECT CAST(embedding AS text) FROM rag.document_chunks WHERE chunk_id = :chunkId",
            nativeQuery = true)
    String findEmbeddingVectorStringByChunkId(@org.springframework.data.repository.query.Param("chunkId") Long chunkId);

    @org.springframework.data.jpa.repository.Query(
            value = "SELECT embedding <-> CAST(:vec AS vector) FROM rag.document_chunks WHERE chunk_id = :chunkId",
            nativeQuery = true)
    Double calculateCosineDistance(@org.springframework.data.repository.query.Param("chunkId") Long chunkId, @org.springframework.data.repository.query.Param("vec") String vec);
}
