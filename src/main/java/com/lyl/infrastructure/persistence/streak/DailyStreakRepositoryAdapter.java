package com.lyl.infrastructure.persistence.streak;

import com.lyl.domain.streak.DailyStreak;
import com.lyl.domain.streak.DailyStreakRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DailyStreakRepositoryAdapter implements DailyStreakRepository {

    private final SpringDataDailyStreakRepository repository;

    @Override
    public DailyStreak save(DailyStreak dailyStreak) {
        return repository.save(dailyStreak);
    }

    @Override
    public Optional<DailyStreak> findByMemberIdAndStreakDate(Long memberId, LocalDate streakDate) {
        return repository.findByMemberIdAndStreakDateAndDeletedAtIsNull(memberId, streakDate);
    }

    @Override
    public List<DailyStreak> findByMemberIdFrom(Long memberId, LocalDate fromDate) {
        return repository.findFrom(memberId, fromDate);
    }

    @Override
    public List<DailyStreak> findAllByMemberId(Long memberId) {
        return repository.findAllByMemberId(memberId);
    }
}
