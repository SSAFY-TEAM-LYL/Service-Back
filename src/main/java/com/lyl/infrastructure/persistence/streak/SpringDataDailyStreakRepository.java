package com.lyl.infrastructure.persistence.streak;

import com.lyl.domain.streak.DailyStreak;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataDailyStreakRepository extends JpaRepository<DailyStreak, Long> {

    Optional<DailyStreak> findByMemberIdAndStreakDateAndDeletedAtIsNull(Long memberId, LocalDate streakDate);

    @Query("""
            select d
            from DailyStreak d
            where d.deletedAt is null
              and d.member.id = :memberId
              and d.streakDate >= :fromDate
            order by d.streakDate asc
            """)
    List<DailyStreak> findFrom(
            @Param("memberId") Long memberId,
            @Param("fromDate") LocalDate fromDate
    );

    @Query("""
            select d
            from DailyStreak d
            where d.deletedAt is null
              and d.member.id = :memberId
            order by d.streakDate asc
            """)
    List<DailyStreak> findAllByMemberId(@Param("memberId") Long memberId);
}
