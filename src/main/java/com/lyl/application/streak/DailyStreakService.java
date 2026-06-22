package com.lyl.application.streak;

import com.lyl.domain.member.Member;
import com.lyl.domain.member.MemberRepository;
import com.lyl.domain.member.exception.MemberNotFoundException;
import com.lyl.domain.streak.DailyStreak;
import com.lyl.domain.streak.DailyStreakRepository;
import com.lyl.presentation.streak.dto.DailyStreakDayResponse;
import com.lyl.presentation.streak.dto.DailyStreakResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DailyStreakService {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");
    private static final int DEFAULT_DAYS = 183;
    private static final int MAX_DAYS = 370;

    private final MemberRepository memberRepository;
    private final DailyStreakRepository dailyStreakRepository;

    @Transactional
    public void recordTodaySubmission(Long memberId) {
        LocalDate today = LocalDate.now(SERVICE_ZONE);
        recordSubmission(memberId, today);
    }

    @Transactional
    public void recordSubmission(Long memberId, LocalDate streakDate) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        DailyStreak existingStreak = dailyStreakRepository.findByMemberIdAndStreakDate(memberId, streakDate)
                .orElse(null);
        if (existingStreak != null) {
            existingStreak.increaseSubmissionCount();
            return;
        }
        dailyStreakRepository.save(new DailyStreak(member, streakDate));
    }

    @Transactional(readOnly = true)
    public DailyStreakResponse findMyStreak(Long memberId, Integer days) {
        int rangeDays = normalizeDays(days);
        LocalDate today = LocalDate.now(SERVICE_ZONE);
        LocalDate startDate = today.minusDays(rangeDays - 1L);

        List<DailyStreak> rangeStreaks = dailyStreakRepository.findByMemberIdFrom(memberId, startDate);
        Map<LocalDate, DailyStreak> streaksByDate = rangeStreaks.stream()
                .collect(Collectors.toMap(DailyStreak::getStreakDate, Function.identity()));
        List<LocalDate> allStreakDates = dailyStreakRepository.findAllByMemberId(memberId).stream()
                .map(DailyStreak::getStreakDate)
                .toList();
        Set<LocalDate> allStreakDateSet = new HashSet<>(allStreakDates);

        List<DailyStreakDayResponse> dayResponses = startDate.datesUntil(today.plusDays(1))
                .map(date -> {
                    DailyStreak streak = streaksByDate.get(date);
                    int submissionCount = streak == null ? 0 : streak.getSubmissionCount();
                    return new DailyStreakDayResponse(date, submissionCount > 0, submissionCount);
                })
                .toList();

        return new DailyStreakResponse(
                calculateCurrentStreak(allStreakDateSet, today),
                calculateLongestStreak(allStreakDates),
                allStreakDates.size(),
                allStreakDateSet.contains(today),
                SERVICE_ZONE.getId(),
                startDate,
                today,
                dayResponses
        );
    }

    private int normalizeDays(Integer days) {
        if (days == null) {
            return DEFAULT_DAYS;
        }
        return Math.min(Math.max(days, 1), MAX_DAYS);
    }

    private int calculateCurrentStreak(Set<LocalDate> streakDates, LocalDate today) {
        int count = 0;
        LocalDate cursor = today;
        while (streakDates.contains(cursor)) {
            count++;
            cursor = cursor.minusDays(1);
        }
        return count;
    }

    private int calculateLongestStreak(List<LocalDate> streakDates) {
        int longest = 0;
        int current = 0;
        LocalDate previous = null;

        for (LocalDate date : streakDates) {
            if (previous == null || date.equals(previous.plusDays(1))) {
                current++;
            } else if (!date.equals(previous)) {
                current = 1;
            }
            longest = Math.max(longest, current);
            previous = date;
        }
        return longest;
    }
}
