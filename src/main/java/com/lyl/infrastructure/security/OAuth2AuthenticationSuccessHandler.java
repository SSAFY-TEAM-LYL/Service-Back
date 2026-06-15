package com.lyl.infrastructure.security;

import com.lyl.application.auth.OAuthLoginCodeService;
import com.lyl.application.auth.OAuthLoginService;
import com.lyl.common.exception.BusinessException;
import com.lyl.common.exception.ErrorCode;
import com.lyl.domain.member.Member;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements org.springframework.security.web.authentication.AuthenticationSuccessHandler {

    private final OAuthLoginService oauthLoginService;
    private final OAuthLoginCodeService oauthLoginCodeService;
    private final OAuth2Properties oauth2Properties;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        try {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            Member member = oauthLoginService.login(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getPrincipal()
            );
            String code = oauthLoginCodeService.createCode(member);
            response.sendRedirect(successUri(code));
        } catch (BusinessException e) {
            ErrorCode errorCode = e.getErrorCode();
            log.warn("OAuth2 login failed after provider authentication. code={}", errorCode.name(), e);
            response.sendRedirect(failureUri(errorCode));
        } catch (RuntimeException e) {
            log.warn("OAuth2 login failed after provider authentication", e);
            response.sendRedirect(failureUri(ErrorCode.OAUTH_LOGIN_FAILED));
        }
    }

    private String successUri(String code) {
        return UriComponentsBuilder.fromUriString(oauth2Properties.successRedirectUri())
                .queryParam("code", code)
                .build()
                .toUriString();
    }

    private String failureUri(ErrorCode errorCode) {
        return UriComponentsBuilder.fromUriString(oauth2Properties.failureRedirectUri())
                .queryParam("error", errorCode.name())
                .build()
                .toUriString();
    }
}
