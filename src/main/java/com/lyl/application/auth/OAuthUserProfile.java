package com.lyl.application.auth;

import com.lyl.domain.member.OAuthProvider;
import com.lyl.domain.member.exception.OAuthEmailRequiredException;
import java.util.Map;

public record OAuthUserProfile(
        OAuthProvider provider,
        String providerUserId,
        String email,
        String nickname
) {

    public static OAuthUserProfile from(OAuthProvider provider, Map<String, Object> attributes) {
        return switch (provider) {
            case GOOGLE -> fromGoogle(attributes);
            case KAKAO -> fromKakao(attributes);
        };
    }

    private static OAuthUserProfile fromGoogle(Map<String, Object> attributes) {
        String providerUserId = asString(attributes.get("sub"));
        String email = asString(attributes.get("email"));
        String nickname = asString(attributes.get("name"));
        validateEmail(email);
        return new OAuthUserProfile(OAuthProvider.GOOGLE, providerUserId, email, defaultNickname(nickname, email));
    }

    @SuppressWarnings("unchecked")
    private static OAuthUserProfile fromKakao(Map<String, Object> attributes) {
        String providerUserId = asString(attributes.get("id"));
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.getOrDefault("kakao_account", Map.of());
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.getOrDefault("profile", Map.of());

        String email = asString(kakaoAccount.get("email"));
        String nickname = asString(profile.get("nickname"));
        validateEmail(email);
        return new OAuthUserProfile(OAuthProvider.KAKAO, providerUserId, email, defaultNickname(nickname, email));
    }

    private static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new OAuthEmailRequiredException();
        }
    }

    private static String defaultNickname(String nickname, String email) {
        if (nickname != null && !nickname.isBlank()) {
            return nickname.length() > 30 ? nickname.substring(0, 30) : nickname;
        }
        String localPart = email.split("@", 2)[0];
        return localPart.length() > 30 ? localPart.substring(0, 30) : localPart;
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
