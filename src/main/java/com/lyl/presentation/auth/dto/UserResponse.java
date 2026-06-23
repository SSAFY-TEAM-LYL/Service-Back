package com.lyl.presentation.auth.dto;

import com.lyl.domain.member.Member;
import com.lyl.domain.member.Role;

public record UserResponse(
        Long id,
        String email,
        String nickname,
        String profileImageUrl,
        Role role,
        int xp,
        int level
) {

    public static UserResponse from(Member member) {
        return new UserResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getProfileImageUrl(),
                member.getRole(),
                member.getXp(),
                member.getLevel()
        );
    }
}
