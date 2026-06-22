package com.lyl.presentation.streak;

import com.lyl.application.streak.DailyStreakService;
import com.lyl.infrastructure.security.UserPrincipal;
import com.lyl.presentation.common.ApiResponse;
import com.lyl.presentation.streak.dto.DailyStreakResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DailyStreakController {

    private final DailyStreakService dailyStreakService;

    @GetMapping("/api/streaks/me")
    public ResponseEntity<ApiResponse<DailyStreakResponse>> findMyStreak(
            @RequestParam(required = false) Integer days,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                dailyStreakService.findMyStreak(userPrincipal.getId(), days)
        ));
    }
}
