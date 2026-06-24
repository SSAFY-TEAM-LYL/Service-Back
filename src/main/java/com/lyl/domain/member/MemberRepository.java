package com.lyl.domain.member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    Member save(Member member);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByEmailIncludingDeleted(String email);

    Optional<Member> findById(Long id);

    List<Member> findRankingPage(int page, int size);

    long countActiveMembers();

    boolean existsByEmail(String email);
}
