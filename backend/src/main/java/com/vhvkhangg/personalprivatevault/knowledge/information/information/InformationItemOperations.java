package com.vhvkhangg.personalprivatevault.knowledge.information.information;

import com.vhvkhangg.personalprivatevault.knowledge.information.view.InformationItemView;

import java.util.Optional;

/**
 * Public capability-oriented contract for Information item operations.
 */
public interface InformationItemOperations {

    InformationItemView create(CreateInformationItemCommand command);

    InformationItemView update(Long id, UpdateInformationItemCommand command);

    Optional<InformationItemView> findById(Long id);
}
