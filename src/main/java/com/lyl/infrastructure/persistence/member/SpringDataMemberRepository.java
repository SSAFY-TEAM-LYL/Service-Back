package com.lyl.infrastructure.persistence.member;

import com.lyl.domain.member.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataMemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByEmailAndDeletedAtIsNull(String email);

    Optional<Member> findByIdAndDeletedAtIsNull(Long id);

    List<Member> findByDeletedAtIsNullOrderByXpDescIdAsc(Pageable pageable);

    @Query("""
            select count(m)
            from Member m
            where m.deletedAt is null
              and (m.xp > :xp or (m.xp = :xp and m.id < :memberId))
            """)
    long countActiveMembersAheadOf(@Param("xp") int xp, @Param("memberId") Long memberId);

    long countByDeletedAtIsNull();

    boolean existsByEmailAndDeletedAtIsNull(String email);
}
