package com.lyl.presentation.member.dto;

import jakarta.validation.constraints.Size;

public record MemberUpdateRequest(
        @Size(max = 30, message = "닉네임은 30자 이하여야 합니다.")
        String nickname,

        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
        String password,

        @Size(max = 500, message = "프로필 이미지 URL은 500자 이하여야 합니다.")
        String profileImageUrl
) {
}
