package com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagDependency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RagDependencyRepository extends JpaRepository<RagDependency, Long> {

    List<RagDependency> findByParentSourceId(Long parentSourceId);

    List<RagDependency> findByChildSourceId(Long childSourceId);

    java.util.Optional<RagDependency> findByParentSourceIdAndChildSourceId(Long parentSourceId, Long childSourceId);
}
