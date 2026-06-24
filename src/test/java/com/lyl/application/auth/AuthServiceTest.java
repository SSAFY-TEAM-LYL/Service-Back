package com.lyl.application.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lyl.domain.member.Member;
import com.lyl.domain.member.MemberRepository;
import com.lyl.domain.member.OAuthAccount;
import com.lyl.domain.member.OAuthAccountRepository;
import com.lyl.domain.member.OAuthProvider;
import com.lyl.domain.member.exception.DeletedMemberException;
import com.lyl.domain.member.exception.DuplicateEmailException;
import com.lyl.domain.member.exception.OAuthAccountConflictException;
import com.lyl.domain.member.exception.OAuthLoginCodeException;
import com.lyl.application.member.MemberService;
import com.lyl.presentation.auth.dto.AuthResponse;
import com.lyl.presentation.auth.dto.LoginRequest;
import com.lyl.presentation.auth.dto.RestoreMemberRequest;
import com.lyl.presentation.auth.dto.SignupRequest;
import com.lyl.presentation.auth.dto.UserResponse;
import com.lyl.presentation.member.dto.MemberUpdateRequest;
import com.lyl.presentation.member.dto.MemberWithdrawalRequest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OAuthLoginService oauthLoginService;

    @Autowired
    private OAuthLoginCodeService oauthLoginCodeService;

    @Autowired
    private OAuthAccountRepository oauthAccountRepository;

    @Autowired
    private MemberService memberService;

    @Test
    void signupThrowsDuplicateEmailWhenEmailAlreadyExists() {
        memberRepository.save(new Member(
                "duplicate@example.com",
                "existingUser",
                passwordEncoder.encode("password123")
        ));

        SignupRequest request = new SignupRequest("newUser", "duplicate@example.com", "password123");

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("이미 가입된 이메일입니다.");
    }

    @Test
    void loginReturnsTokenAndUserWhenCredentialsAreValid() {
        Member member = memberRepository.save(new Member(
                "login-success@example.com",
                "loginUser",
                passwordEncoder.encode("password123")
        ));

        AuthResponse response = authService.login(new LoginRequest("login-success@example.com", "password123"));

        assertThat(response.token()).isNotBlank();
        assertThat(response.user().id()).isEqualTo(member.getId());
        assertThat(response.user().email()).isEqualTo(member.getEmail());
        assertThat(response.user().nickname()).isEqualTo(member.getNickname());
    }

    @Test
    void memberLevelIsCalculatedFromXp() {
        Member member = new Member(
                "level-calc@example.com",
                "levelUser",
                passwordEncoder.encode("password123")
        );
        member.addXp(120);
        memberRepository.save(member);

        UserResponse response = memberService.getMe(member.getId());

        assertThat(response.xp()).isEqualTo(120);
        assertThat(response.level()).isEqualTo(3);
    }

    @Test
    void findRankingsOrdersActiveMembersByXpDescending() {
        Member first = new Member("rank-first@example.com", "first", passwordEncoder.encode("password123"));
        first.addXp(150);
        Member second = new Member("rank-second@example.com", "second", passwordEncoder.encode("password123"));
        second.addXp(80);
        Member deleted = new Member("rank-deleted@example.com", "deleted", passwordEncoder.encode("password123"));
        deleted.addXp(300);
        deleted.delete();
        memberRepository.save(second);
        memberRepository.save(first);
        memberRepository.save(deleted);

        var response = memberService.findRankings(0, 10);

        assertThat(response.items())
                .extracting(item -> item.rank(), item -> item.nickname(), item -> item.xp(), item -> item.level())
                .containsSubsequence(
                        org.assertj.core.groups.Tuple.tuple(1, "first", 150, 4),
                        org.assertj.core.groups.Tuple.tuple(2, "second", 80, 2)
                );
        assertThat(response.items())
                .extracting(item -> item.nickname())
                .doesNotContain("deleted");
    }

    @Test
    void loginThrowsBadCredentialsWhenPasswordIsInvalid() {
        memberRepository.save(new Member(
                "login-fail@example.com",
                "failUser",
                passwordEncoder.encode("password123")
        ));

        assertThatThrownBy(() -> authService.login(new LoginRequest("login-fail@example.com", "wrong-password")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void signupThrowsDeletedMemberWhenEmailBelongsToDeletedMember() {
        Member member = memberRepository.save(new Member(
                "deleted-signup@example.com",
                "deletedUser",
                passwordEncoder.encode("password123")
        ));
        member.delete();
        memberRepository.save(member);

        SignupRequest request = new SignupRequest("newUser", "deleted-signup@example.com", "password123");

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(DeletedMemberException.class)
                .hasMessage("탈퇴처리한 회원입니다. 계정 복구 후 이용해주세요.");
    }

    @Test
    void loginThrowsDeletedMemberWhenEmailBelongsToDeletedMember() {
        Member member = memberRepository.save(new Member(
                "deleted-login@example.com",
                "deletedLoginUser",
                passwordEncoder.encode("password123")
        ));
        member.delete();
        memberRepository.save(member);

        assertThatThrownBy(() -> authService.login(new LoginRequest("deleted-login@example.com", "password123")))
                .isInstanceOf(DeletedMemberException.class)
                .hasMessage("탈퇴처리한 회원입니다. 계정 복구 후 이용해주세요.");
    }

    @Test
    void restoreDeletedMemberReturnsTokenAndUserWhenPasswordMatches() {
        Member member = memberRepository.save(new Member(
                "restore@example.com",
                "restoreUser",
                passwordEncoder.encode("password123")
        ));
        member.delete();
        memberRepository.save(member);

        AuthResponse response = authService.restore(new RestoreMemberRequest("restore@example.com", "password123"));

        assertThat(response.token()).isNotBlank();
        assertThat(response.user().email()).isEqualTo("restore@example.com");
        assertThat(memberRepository.findByEmail("restore@example.com")).isPresent();
    }

    @Test
    void updateProfileChangesNicknamePasswordAndProfileImageUrl() {
        Member member = memberRepository.save(new Member(
                "profile-update@example.com",
                "before",
                passwordEncoder.encode("password123")
        ));

        UserResponse response = memberService.updateMe(
                member.getId(),
                new MemberUpdateRequest("after", "newPassword123", "https://example.com/profile.png")
        );

        assertThat(response.nickname()).isEqualTo("after");
        assertThat(response.profileImageUrl()).isEqualTo("https://example.com/profile.png");
        assertThat(authService.login(new LoginRequest("profile-update@example.com", "newPassword123")).token())
                .isNotBlank();
    }

    @Test
    void withdrawSoftDeletesMemberAndBlocksLogin() {
        Member member = memberRepository.save(new Member(
                "withdraw@example.com",
                "withdrawUser",
                passwordEncoder.encode("password123")
        ));

        memberService.withdrawMe(member.getId(), new MemberWithdrawalRequest("password123"));

        assertThat(memberRepository.findByEmail("withdraw@example.com")).isEmpty();
        assertThat(memberRepository.findByEmailIncludingDeleted("withdraw@example.com"))
                .isPresent()
                .get()
                .extracting(Member::isDeleted)
                .isEqualTo(true);
        assertThatThrownBy(() -> authService.login(new LoginRequest("withdraw@example.com", "password123")))
                .isInstanceOf(DeletedMemberException.class);
    }

    @Test
    void withdrawSoftDeletesOAuthMemberWithoutPassword() {
        Member member = memberRepository.save(new Member(
                "oauth-withdraw@example.com",
                "oauthWithdrawUser",
                passwordEncoder.encode("password123")
        ));
        oauthAccountRepository.save(new OAuthAccount(
                member,
                OAuthProvider.GOOGLE,
                "google-withdraw-sub",
                "oauth-withdraw@example.com"
        ));

        memberService.withdrawMe(member.getId(), new MemberWithdrawalRequest(null));

        assertThat(memberRepository.findByEmail("oauth-withdraw@example.com")).isEmpty();
        assertThat(memberRepository.findByEmailIncludingDeleted("oauth-withdraw@example.com"))
                .isPresent()
                .get()
                .extracting(Member::isDeleted)
                .isEqualTo(true);
    }

    @Test
    void oauthLoginCreatesMemberAndOAuthAccountWhenEmailIsNew() {
        OAuth2User oauth2User = googleUser(
                "google-sub-1",
                "google-new@example.com",
                "googleUser",
                "https://lh3.googleusercontent.com/profile.png"
        );

        Member member = oauthLoginService.login("google", oauth2User);

        assertThat(member.getEmail()).isEqualTo("google-new@example.com");
        assertThat(member.getNickname()).isEqualTo("googleUser");
        assertThat(member.getProfileImageUrl()).isEqualTo("https://lh3.googleusercontent.com/profile.png");
        assertThat(oauthAccountRepository.findByProviderAndProviderUserId(OAuthProvider.GOOGLE, "google-sub-1"))
                .isPresent()
                .get()
                .extracting(account -> account.getMember().getId())
                .isEqualTo(member.getId());
    }

    @Test
    void oauthLoginCreatesKakaoMemberWithProfileImageUrl() {
        OAuth2User oauth2User = kakaoUser(
                "kakao-sub-1",
                "kakao-new@example.com",
                "kakaoUser",
                "https://k.kakaocdn.net/profile.png"
        );

        Member member = oauthLoginService.login("kakao", oauth2User);

        assertThat(member.getEmail()).isEqualTo("kakao-new@example.com");
        assertThat(member.getNickname()).isEqualTo("kakaoUser");
        assertThat(member.getProfileImageUrl()).isEqualTo("https://k.kakaocdn.net/profile.png");
    }

    @Test
    void oauthLoginCreatesKakaoMemberWithPropertiesProfileImageFallback() {
        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of(
                        "id", "kakao-sub-properties",
                        "properties", Map.of(
                                "nickname", "kakaoPropertiesUser",
                                "profile_image", "https://k.kakaocdn.net/properties-profile.png"
                        ),
                        "kakao_account", Map.of(
                                "email", "kakao-properties@example.com",
                                "profile", Map.of(
                                        "nickname", "kakaoPropertiesUser"
                                )
                        )
                ),
                "id"
        );

        Member member = oauthLoginService.login("kakao", oauth2User);

        assertThat(member.getProfileImageUrl()).isEqualTo("https://k.kakaocdn.net/properties-profile.png");
    }

    @Test
    void oauthRestoreRestoresDeletedLinkedMember() {
        Member member = memberRepository.save(new Member(
                "oauth-restore@example.com",
                "oauthRestoreUser",
                passwordEncoder.encode("password123")
        ));
        oauthAccountRepository.save(new OAuthAccount(
                member,
                OAuthProvider.GOOGLE,
                "google-restore-sub",
                "oauth-restore@example.com"
        ));
        member.delete();
        memberRepository.save(member);

        Member restored = oauthLoginService.restore(
                "google",
                googleUser(
                        "google-restore-sub",
                        "oauth-restore@example.com",
                        "oauthRestoreUser",
                        "https://lh3.googleusercontent.com/restored.png"
                )
        );

        assertThat(restored.isDeleted()).isFalse();
        assertThat(restored.getProfileImageUrl()).isEqualTo("https://lh3.googleusercontent.com/restored.png");
        assertThat(memberRepository.findByEmail("oauth-restore@example.com")).isPresent();
    }

    @Test
    void oauthLoginThrowsConflictWhenLocalAccountUsesSameEmail() {
        memberRepository.save(new Member(
                "oauth-conflict@example.com",
                "localUser",
                passwordEncoder.encode("password123")
        ));

        assertThatThrownBy(() -> oauthLoginService.login(
                "google",
                googleUser("google-sub-2", "oauth-conflict@example.com", "googleUser")
        )).isInstanceOf(OAuthAccountConflictException.class);
    }

    @Test
    void oauthLoginThrowsDeletedMemberWhenLinkedMemberIsDeleted() {
        Member member = memberRepository.save(new Member(
                "oauth-deleted@example.com",
                "oauthDeletedUser",
                passwordEncoder.encode("password123")
        ));
        oauthAccountRepository.save(new OAuthAccount(
                member,
                OAuthProvider.GOOGLE,
                "google-deleted-sub",
                "oauth-deleted@example.com"
        ));
        member.delete();
        memberRepository.save(member);

        assertThatThrownBy(() -> oauthLoginService.login(
                "google",
                googleUser("google-deleted-sub", "oauth-deleted@example.com", "googleUser")
        )).isInstanceOf(DeletedMemberException.class);
    }

    @Test
    void oauthLoginCodeCanBeExchangedOnlyOnce() {
        Member member = memberRepository.save(new Member(
                "oauth-code@example.com",
                "oauthCodeUser",
                passwordEncoder.encode("password123")
        ));
        String code = oauthLoginCodeService.createCode(member);

        AuthResponse response = oauthLoginCodeService.exchange(code);

        assertThat(response.token()).isNotBlank();
        assertThat(response.user().id()).isEqualTo(member.getId());
        assertThatThrownBy(() -> oauthLoginCodeService.exchange(code))
                .isInstanceOf(OAuthLoginCodeException.class);
    }

    private OAuth2User googleUser(String subject, String email, String nickname) {
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of(
                        "sub", subject,
                        "email", email,
                        "name", nickname
                ),
                "sub"
        );
    }

    private OAuth2User googleUser(String subject, String email, String nickname, String profileImageUrl) {
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of(
                        "sub", subject,
                        "email", email,
                        "name", nickname,
                        "picture", profileImageUrl
                ),
                "sub"
        );
    }

    private OAuth2User kakaoUser(String subject, String email, String nickname, String profileImageUrl) {
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of(
                        "id", subject,
                        "kakao_account", Map.of(
                                "email", email,
                                "profile", Map.of(
                                        "nickname", nickname,
                                        "profile_image_url", profileImageUrl
                                )
                        )
                ),
                "id"
        );
    }
}
