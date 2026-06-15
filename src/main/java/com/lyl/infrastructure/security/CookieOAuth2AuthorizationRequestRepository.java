package com.lyl.infrastructure.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

@Component
public class CookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    private static final String OAUTH2_RESTORE_MODE_COOKIE_NAME = "oauth2_restore_mode";
    private static final String RESTORE_MODE_PARAMETER = "mode";
    private static final String RESTORE_MODE_VALUE = "restore";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return getCookie(request)
                .map(cookie -> deserialize(cookie.getValue()))
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(response);
            return;
        }

        Cookie cookie = new Cookie(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, serialize(authorizationRequest));
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(COOKIE_EXPIRE_SECONDS);
        response.addCookie(cookie);

        if (RESTORE_MODE_VALUE.equals(request.getParameter(RESTORE_MODE_PARAMETER))) {
            Cookie restoreCookie = new Cookie(OAUTH2_RESTORE_MODE_COOKIE_NAME, "true");
            restoreCookie.setPath("/");
            restoreCookie.setHttpOnly(true);
            restoreCookie.setMaxAge(COOKIE_EXPIRE_SECONDS);
            response.addCookie(restoreCookie);
        } else {
            Cookie restoreCookie = new Cookie(OAUTH2_RESTORE_MODE_COOKIE_NAME, "");
            restoreCookie.setPath("/");
            restoreCookie.setHttpOnly(true);
            restoreCookie.setMaxAge(0);
            response.addCookie(restoreCookie);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        removeAuthorizationRequestCookies(response);
        return authorizationRequest;
    }

    private java.util.Optional<Cookie> getCookie(HttpServletRequest request) {
        return getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
    }

    public boolean isRestoreMode(HttpServletRequest request) {
        return getCookie(request, OAUTH2_RESTORE_MODE_COOKIE_NAME)
                .map(cookie -> "true".equals(cookie.getValue()))
                .orElse(false);
    }

    private java.util.Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return java.util.Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .findFirst();
    }

    private void removeAuthorizationRequestCookies(HttpServletResponse response) {
        Cookie cookie = new Cookie(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        Cookie restoreCookie = new Cookie(OAUTH2_RESTORE_MODE_COOKIE_NAME, "");
        restoreCookie.setPath("/");
        restoreCookie.setHttpOnly(true);
        restoreCookie.setMaxAge(0);
        response.addCookie(restoreCookie);
    }

    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        byte[] bytes = SerializationUtils.serialize(authorizationRequest);
        return Base64.getUrlEncoder().encodeToString(bytes);
    }

    private OAuth2AuthorizationRequest deserialize(String value) {
        byte[] bytes = Base64.getUrlDecoder().decode(value.getBytes(StandardCharsets.UTF_8));
        return (OAuth2AuthorizationRequest) SerializationUtils.deserialize(bytes);
    }
}
