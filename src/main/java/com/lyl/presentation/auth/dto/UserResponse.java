package com.lyl.presentation.auth.dto;

import com.lyl.domain.member.Member;

public record UserResponse(
        Long id,
        String email,
        String nickname
) {

    public static UserResponse from(Member member) {
        return new UserResponse(member.getId(), member.getEmail(), member.getNickname());
    }
}
