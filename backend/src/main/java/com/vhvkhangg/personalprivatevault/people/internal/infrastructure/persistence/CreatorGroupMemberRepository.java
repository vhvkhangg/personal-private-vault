package com.vhvkhangg.personalprivatevault.people.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.people.internal.domain.CreatorGroupMember;
import com.vhvkhangg.personalprivatevault.people.internal.domain.CreatorGroupMemberId;
import com.vhvkhangg.personalprivatevault.people.view.CreatorGroupMemberView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for {@link CreatorGroupMember} join entities.
 */
public interface CreatorGroupMemberRepository extends JpaRepository<CreatorGroupMember, CreatorGroupMemberId> {

    List<CreatorGroupMember> findByIdCreatorGroupId(Long creatorGroupId);

    boolean existsByIdCreatorGroupIdAndIdPersonId(Long creatorGroupId, Long personId);

    @Query("""
            SELECT new com.vhvkhangg.personalprivatevault.people.view.CreatorGroupMemberView(
                m.id.creatorGroupId,
                p.id,
                p.name
            )
            FROM CreatorGroupMember m, Person p
            WHERE m.id.personId = p.id
              AND m.id.creatorGroupId = :creatorGroupId
            ORDER BY p.name ASC, p.id ASC
            """)
    List<CreatorGroupMemberView> findMembersByGroupId(@Param("creatorGroupId") Long creatorGroupId);

    @Modifying(flushAutomatically = true)
    @Query(
            value = "INSERT INTO creator_group_members (creator_group_id, person_id) VALUES (:groupId, :personId) ON CONFLICT (creator_group_id, person_id) DO NOTHING",
            nativeQuery = true
    )
    void insertMemberIfAbsent(@Param("groupId") Long groupId, @Param("personId") Long personId);
}
