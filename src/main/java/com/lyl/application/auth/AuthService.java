package com.lyl.application.auth;

import com.lyl.domain.member.Member;
import com.lyl.domain.member.MemberRepository;
import com.lyl.domain.member.exception.DuplicateEmailException;
import com.lyl.infrastructure.security.JwtTokenProvider;
import com.lyl.infrastructure.security.UserPrincipal;
import com.lyl.presentation.auth.dto.AuthResponse;
import com.lyl.presentation.auth.dto.LoginRequest;
import com.lyl.presentation.auth.dto.SignupRequest;
import com.lyl.presentation.auth.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException();
        }

        Member member = new Member(
                request.email(),
                request.nickname(),
                passwordEncoder.encode(request.password())
        );
        Member savedMember = memberRepository.save(member);
        String token = jwtTokenProvider.generateToken(savedMember);

        return new AuthResponse(token, UserResponse.from(savedMember));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        String token = jwtTokenProvider.generateToken(userPrincipal);
        UserResponse user = new UserResponse(
                userPrincipal.getId(),
                userPrincipal.getEmail(),
                userPrincipal.getNickname()
        );
        return new AuthResponse(token, user);
    }
}
