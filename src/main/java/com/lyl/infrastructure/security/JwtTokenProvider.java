package com.lyl.infrastructure.security;

import com.lyl.domain.member.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Member member) {
        return generateToken(member.getEmail(), member.getId(), member.getNickname());
    }

    public String generateToken(UserPrincipal userPrincipal) {
        return generateToken(userPrincipal.getEmail(), userPrincipal.getId(), userPrincipal.getNickname());
    }

    private String generateToken(String subject, Long memberId, String nickname) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + jwtProperties.expirationMillis());

        return Jwts.builder()
                .subject(subject)
                .claim("memberId", memberId)
                .claim("nickname", nickname)
                .issuedAt(now)
                .expiration(expiresAt)
                .signWith(secretKey)
                .compact();
    }

    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        parseClaims(token);
        return true;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
