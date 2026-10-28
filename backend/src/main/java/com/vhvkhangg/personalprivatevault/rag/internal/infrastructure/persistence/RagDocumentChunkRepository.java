package com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagDocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface RagDocumentChunkRepository extends JpaRepository<RagDocumentChunk, Long> {

    List<RagDocumentChunk> findByGenerationId(Long generationId);

    long countByGenerationId(Long generationId);

    long countByGenerationIdIn(Collection<Long> generationIds);

    List<RagDocumentChunk> findByGenerationIdIn(Collection<Long> generationIds);
    List<RagDocumentChunk> findByGenerationIdIn(Collection<Long> generationIds,
            org.springframework.data.domain.Pageable pageable);

    @Query(
            value = "SELECT CAST(embedding AS text) FROM rag.document_chunks WHERE chunk_id = :chunkId",
            nativeQuery = true)
    String findEmbeddingVectorStringByChunkId(@Param("chunkId") Long chunkId);

    @Query(
            value = "SELECT embedding <-> CAST(:vec AS vector) FROM rag.document_chunks WHERE chunk_id = :chunkId",
            nativeQuery = true)
    Double calculateCosineDistance(@Param("chunkId") Long chunkId, @Param("vec") String vec);

    interface ChunkRankProjection {
        Long getChunkId();
        Double getDistance();
    }

    @Query(
            value = """
                    SELECT c.chunk_id AS chunkId,
                           (c.embedding <=> CAST(:queryVector AS vector)) AS distance
                    FROM rag.document_chunks c
                    WHERE c.chunk_id IN (:chunkIds)
                      AND c.embedding IS NOT NULL
                      AND (c.embedding <=> CAST(:queryVector AS vector)) < 'Infinity'::float8
                    ORDER BY (c.embedding <=> CAST(:queryVector AS vector)) ASC,
                             c.source_identity ASC, c.generation_id ASC, c.chunk_id ASC
                    LIMIT :topK
                    """,
            nativeQuery = true)
    List<ChunkRankProjection> findRankedByCosineDistance(
            @Param("chunkIds") Collection<Long> chunkIds,
            @Param("queryVector") String queryVector,
            @Param("topK") int topK
    );
}
