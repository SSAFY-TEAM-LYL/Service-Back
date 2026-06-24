package com.lyl.presentation.member;

import com.lyl.application.member.MemberService;
import com.lyl.application.member.MemberSolvedStatsService;
import com.lyl.infrastructure.security.UserPrincipal;
import com.lyl.presentation.auth.dto.UserResponse;
import com.lyl.presentation.common.ApiResponse;
import com.lyl.presentation.member.dto.MemberRankingSummaryResponse;
import com.lyl.presentation.member.dto.MemberSolvedStatsResponse;
import com.lyl.presentation.member.dto.MemberUpdateRequest;
import com.lyl.presentation.member.dto.MemberWithdrawalRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberSolvedStatsService solvedStatsService;

    @GetMapping("/rankings")
    public ResponseEntity<ApiResponse<MemberRankingSummaryResponse>> getRankings(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long currentMemberId = userPrincipal == null ? null : userPrincipal.getId();
        return ResponseEntity.ok(ApiResponse.success(memberService.findRankings(currentMemberId)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getMe(userPrincipal.getId())));
    }

    @GetMapping("/me/solved-stats")
    public ResponseEntity<ApiResponse<MemberSolvedStatsResponse>> getMySolvedStats(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(ApiResponse.success(solvedStatsService.findMySolvedStats(userPrincipal.getId())));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody MemberUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.updateMe(userPrincipal.getId(), request)));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdrawMe(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody MemberWithdrawalRequest request
    ) {
        memberService.withdrawMe(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
