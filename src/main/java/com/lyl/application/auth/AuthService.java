package com.lyl.application.auth;

import com.lyl.domain.member.Member;
import com.lyl.domain.member.MemberRepository;
import com.lyl.domain.member.exception.DeletedMemberException;
import com.lyl.domain.member.exception.DuplicateEmailException;
import com.lyl.infrastructure.security.JwtTokenProvider;
import com.lyl.infrastructure.security.UserPrincipal;
import com.lyl.presentation.auth.dto.AuthResponse;
import com.lyl.presentation.auth.dto.LoginRequest;
import com.lyl.presentation.auth.dto.RestoreMemberRequest;
import com.lyl.presentation.auth.dto.SignupRequest;
import com.lyl.presentation.auth.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
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
        memberRepository.findByEmailIncludingDeleted(request.email())
                .ifPresent(member -> {
                    if (member.isDeleted()) {
                        throw new DeletedMemberException();
                    }
                    throw new DuplicateEmailException();
                });

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
        memberRepository.findByEmailIncludingDeleted(request.email())
                .filter(Member::isDeleted)
                .ifPresent(member -> {
                    throw new DeletedMemberException();
                });

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        String token = jwtTokenProvider.generateToken(userPrincipal);
        UserResponse user = new UserResponse(
                userPrincipal.getId(),
                userPrincipal.getEmail(),
                userPrincipal.getNickname(),
                userPrincipal.getProfileImageUrl(),
                userPrincipal.getRole(),
                userPrincipal.getXp(),
                userPrincipal.getLevel()
        );
        return new AuthResponse(token, user);
    }

    @Transactional
    public AuthResponse restore(RestoreMemberRequest request) {
        Member member = memberRepository.findByEmailIncludingDeleted(request.email())
                .filter(Member::isDeleted)
                .orElseThrow(() -> new BadCredentialsException("복구할 탈퇴 회원을 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        member.restore();
        String token = jwtTokenProvider.generateToken(member);
        return new AuthResponse(token, UserResponse.from(member));
    }
}
