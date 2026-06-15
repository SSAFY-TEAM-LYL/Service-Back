package com.lyl.domain.member;

public enum OAuthProvider {
    GOOGLE,
    KAKAO;

    public static OAuthProvider fromRegistrationId(String registrationId) {
        return OAuthProvider.valueOf(registrationId.toUpperCase());
    }
}
