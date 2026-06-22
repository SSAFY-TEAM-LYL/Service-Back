package com.lyl.application.streak;

import static org.assertj.core.api.Assertions.assertThat;

import com.lyl.domain.member.Member;
import com.lyl.domain.member.MemberRepository;
import com.lyl.domain.streak.DailyStreakRepository;
import com.lyl.presentation.streak.dto.DailyStreakResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DailyStreakServiceTest {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    @Autowired
    private DailyStreakService dailyStreakService;

    @Autowired
    private DailyStreakRepository dailyStreakRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void recordSubmissionStoresOnlyOneStreakPerDay() {
        Member member = saveMember("streak1@example.com", "streak1");
        LocalDate today = LocalDate.now(SERVICE_ZONE);

        dailyStreakService.recordSubmission(member.getId(), today);
        dailyStreakService.recordSubmission(member.getId(), today);

        DailyStreakResponse response = dailyStreakService.findMyStreak(member.getId(), 7);
        assertThat(response.totalActiveDays()).isEqualTo(1);
        assertThat(response.todaySubmitted()).isTrue();
        assertThat(dailyStreakRepository.findAllByMemberId(member.getId()))
                .extracting(streak -> streak.getStreakDate())
                .containsExactly(today);
        assertThat(response.days())
                .filteredOn(day -> day.date().equals(today))
                .singleElement()
                .satisfies(day -> assertThat(day.submissionCount()).isEqualTo(2));
    }

    @Test
    void findMyStreakCalculatesCurrentAndLongestStreaks() {
        Member member = saveMember("streak2@example.com", "streak2");
        LocalDate today = LocalDate.now(SERVICE_ZONE);
        dailyStreakService.recordSubmission(member.getId(), today.minusDays(5));
        dailyStreakService.recordSubmission(member.getId(), today.minusDays(4));
        dailyStreakService.recordSubmission(member.getId(), today.minusDays(2));
        dailyStreakService.recordSubmission(member.getId(), today.minusDays(1));
        dailyStreakService.recordSubmission(member.getId(), today);

        DailyStreakResponse response = dailyStreakService.findMyStreak(member.getId(), 7);

        assertThat(response.currentStreak()).isEqualTo(3);
        assertThat(response.longestStreak()).isEqualTo(3);
        assertThat(response.days()).hasSize(7);
        assertThat(response.days())
                .filteredOn(day -> day.submitted())
                .hasSize(5);
    }

    private Member saveMember(String email, String nickname) {
        return memberRepository.save(new Member(email, nickname, "encoded-password"));
    }
}
