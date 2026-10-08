package com.vhvkhangg.personalprivatevault.journal.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.journal.internal.domain.DiaryEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for {@link DiaryEntry}.
 */
@Repository
public interface DiaryEntryRepository extends JpaRepository<DiaryEntry, Long> {

    Optional<DiaryEntry> findByIdAndDeletedAtIsNull(Long id);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DiaryEntry d WHERE d.id = :id")
    Optional<DiaryEntry> findByIdForUpdate(@Param("id") Long id);

    List<DiaryEntry> findByDeletedAtIsNullOrderByEntryDateDescIdDesc(Pageable pageable);

    List<DiaryEntry> findByDeletedAtIsNullAndEntryDateGreaterThanEqualOrderByEntryDateDescIdDesc(
            LocalDate fromDate, Pageable pageable);

    List<DiaryEntry> findByDeletedAtIsNullAndEntryDateLessThanEqualOrderByEntryDateDescIdDesc(
            LocalDate toDate, Pageable pageable);

    List<DiaryEntry> findByDeletedAtIsNullAndEntryDateBetweenOrderByEntryDateDescIdDesc(
            LocalDate fromDate, LocalDate toDate, Pageable pageable);
}
