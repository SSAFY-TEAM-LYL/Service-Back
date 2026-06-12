package com.lyl.presentation.auth.dto;

import com.lyl.domain.member.Member;
import com.lyl.domain.member.Role;

public record UserResponse(
        Long id,
        String email,
        String nickname,
        Role role
) {

    public static UserResponse from(Member member) {
        return new UserResponse(member.getId(), member.getEmail(), member.getNickname(), member.getRole());
    }
}
