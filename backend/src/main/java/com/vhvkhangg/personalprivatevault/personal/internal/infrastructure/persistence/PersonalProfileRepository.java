package com.vhvkhangg.personalprivatevault.personal.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.personal.internal.domain.PersonalProfile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for {@link PersonalProfile}.
 */
@Repository
public interface PersonalProfileRepository extends JpaRepository<PersonalProfile, Long> {

    Optional<PersonalProfile> findByIdAndDeletedAtIsNull(Long id);

    Optional<PersonalProfile> findByIsSelfTrueAndDeletedAtIsNull();

    boolean existsByIsSelfTrueAndDeletedAtIsNull();

    boolean existsByIsSelfTrueAndDeletedAtIsNullAndIdNot(Long id);

    List<PersonalProfile> findByDeletedAtIsNullOrderByNameAscIdAsc(Pageable pageable);
}
