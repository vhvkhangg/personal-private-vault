package com.vhvkhangg.personalprivatevault.knowledge.study.study;

import com.vhvkhangg.personalprivatevault.knowledge.study.view.StudyItemView;

import java.util.Optional;

/**
 * Public capability-oriented contract for Study item operations.
 */
public interface StudyItemOperations {

    StudyItemView create(CreateStudyItemCommand command);

    StudyItemView update(Long id, UpdateStudyItemCommand command);

    Optional<StudyItemView> findById(Long id);
}
