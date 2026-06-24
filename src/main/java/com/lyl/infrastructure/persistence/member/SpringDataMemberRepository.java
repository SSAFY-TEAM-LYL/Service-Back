package com.lyl.infrastructure.persistence.member;

import com.lyl.domain.member.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataMemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByEmailAndDeletedAtIsNull(String email);

    Optional<Member> findByIdAndDeletedAtIsNull(Long id);

    List<Member> findByDeletedAtIsNullOrderByXpDescIdAsc(Pageable pageable);

    long countByDeletedAtIsNull();

    boolean existsByEmailAndDeletedAtIsNull(String email);
}
