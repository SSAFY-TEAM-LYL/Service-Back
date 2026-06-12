package com.lyl.infrastructure.security;

import com.lyl.domain.member.Member;
import com.lyl.domain.member.Role;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String nickname;
    private final String password;
    private final Role role;
    private final Collection<? extends GrantedAuthority> authorities;

    private UserPrincipal(Long id, String email, String nickname, String password,
                          Role role, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.role = role;
        this.authorities = authorities;
    }

    public static UserPrincipal from(Member member) {
        return new UserPrincipal(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getPassword(),
                member.getRole(),
                List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()))
        );
    }

    @Override
    public String getUsername() {
        return email;
    }
}
