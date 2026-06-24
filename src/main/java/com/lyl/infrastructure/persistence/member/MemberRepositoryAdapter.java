package com.lyl.infrastructure.persistence.member;

import com.lyl.domain.member.Member;
import com.lyl.domain.member.MemberRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryAdapter implements MemberRepository {

    private final SpringDataMemberRepository repository;

    @Override
    public Member save(Member member) {
        return repository.save(member);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return repository.findByEmailAndDeletedAtIsNull(email);
    }

    @Override
    public Optional<Member> findByEmailIncludingDeleted(String email) {
        return repository.findByEmail(email);
    }

    @Override
    public Optional<Member> findById(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public List<Member> findRankingPage(int page, int size) {
        return repository.findByDeletedAtIsNullOrderByXpDescIdAsc(PageRequest.of(page, size));
    }

    @Override
    public long countActiveMembersAheadOf(int xp, Long memberId) {
        return repository.countActiveMembersAheadOf(xp, memberId);
    }

    @Override
    public long countActiveMembers() {
        return repository.countByDeletedAtIsNull();
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmailAndDeletedAtIsNull(email);
    }
}
