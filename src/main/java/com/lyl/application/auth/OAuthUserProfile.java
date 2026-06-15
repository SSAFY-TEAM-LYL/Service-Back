package com.lyl.application.auth;

import com.lyl.domain.member.OAuthProvider;
import com.lyl.domain.member.exception.OAuthEmailRequiredException;
import java.util.Map;

public record OAuthUserProfile(
        OAuthProvider provider,
        String providerUserId,
        String email,
        String nickname,
        String profileImageUrl
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
        String profileImageUrl = asString(attributes.get("picture"));
        validateEmail(email);
        return new OAuthUserProfile(
                OAuthProvider.GOOGLE,
                providerUserId,
                email,
                defaultNickname(nickname, email),
                blankToNull(profileImageUrl)
        );
    }

    @SuppressWarnings("unchecked")
    private static OAuthUserProfile fromKakao(Map<String, Object> attributes) {
        String providerUserId = asString(attributes.get("id"));
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.getOrDefault("kakao_account", Map.of());
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.getOrDefault("profile", Map.of());

        String email = asString(kakaoAccount.get("email"));
        String nickname = asString(profile.get("nickname"));
        String profileImageUrl = kakaoProfileImage(profile, attributes);
        validateEmail(email);
        return new OAuthUserProfile(
                OAuthProvider.KAKAO,
                providerUserId,
                email,
                defaultNickname(nickname, email),
                blankToNull(profileImageUrl)
        );
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

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    @SuppressWarnings("unchecked")
    private static String kakaoProfileImage(Map<String, Object> profile, Map<String, Object> attributes) {
        String profileImageUrl = asString(profile.get("profile_image_url"));
        if (profileImageUrl == null || profileImageUrl.isBlank()) {
            profileImageUrl = asString(profile.get("thumbnail_image_url"));
        }
        if (profileImageUrl == null || profileImageUrl.isBlank()) {
            Map<String, Object> properties = (Map<String, Object>) attributes.getOrDefault("properties", Map.of());
            profileImageUrl = asString(properties.get("profile_image"));
        }
        if (profileImageUrl == null || profileImageUrl.isBlank()) {
            Map<String, Object> properties = (Map<String, Object>) attributes.getOrDefault("properties", Map.of());
            profileImageUrl = asString(properties.get("thumbnail_image"));
        }
        return profileImageUrl;
    }
}
