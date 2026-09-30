package com.vhvkhangg.personalprivatevault.knowledge.information.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.knowledge.information.internal.domain.InformationItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InformationItemRepository extends JpaRepository<InformationItem, Long> {
}
