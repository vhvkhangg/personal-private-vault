package com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagGenerationRoot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RagGenerationRootRepository extends JpaRepository<RagGenerationRoot, Long> {

    Optional<RagGenerationRoot> findByRootKey(String rootKey);
}
