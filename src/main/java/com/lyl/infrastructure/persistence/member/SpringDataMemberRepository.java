package com.lyl.infrastructure.persistence.member;

import com.lyl.domain.member.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataMemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);
}
