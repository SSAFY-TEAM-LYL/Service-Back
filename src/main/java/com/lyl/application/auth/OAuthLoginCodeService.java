package com.lyl.application.auth;

import com.lyl.domain.auth.OAuthLoginCode;
import com.lyl.domain.auth.OAuthLoginCodeRepository;
import com.lyl.domain.member.Member;
import com.lyl.domain.member.exception.OAuthLoginCodeException;
import com.lyl.infrastructure.security.JwtTokenProvider;
import com.lyl.infrastructure.security.OAuth2Properties;
import com.lyl.presentation.auth.dto.AuthResponse;
import com.lyl.presentation.auth.dto.UserResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuthLoginCodeService {

    private static final int CODE_BYTE_LENGTH = 32;

    private final OAuthLoginCodeRepository oauthLoginCodeRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final OAuth2Properties oauth2Properties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public String createCode(Member member) {
        String code = generateCode();
        OAuthLoginCode loginCode = new OAuthLoginCode(
                hash(code),
                member,
                LocalDateTime.now().plusSeconds(oauth2Properties.codeExpirationSeconds())
        );
        oauthLoginCodeRepository.save(loginCode);
        return code;
    }

    @Transactional
    public AuthResponse exchange(String code) {
        LocalDateTime now = LocalDateTime.now();
        OAuthLoginCode loginCode = oauthLoginCodeRepository.findByCodeHash(hash(code))
                .filter(savedCode -> savedCode.isUsable(now))
                .orElseThrow(OAuthLoginCodeException::new);

        loginCode.use(now);
        Member member = loginCode.getMember();
        return new AuthResponse(jwtTokenProvider.generateToken(member), UserResponse.from(member));
    }

    private String generateCode() {
        byte[] bytes = new byte[CODE_BYTE_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String code) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(code.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }
}
