package com.lyl.domain.member;

import com.lyl.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Column(length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private int xp;

    @Column(nullable = false)
    private int level;

    public Member(String email, String nickname, String password) {
        this(email, nickname, password, Role.USER);
    }

    public Member(String email, String nickname, String password, Role role) {
        this(email, nickname, password, null, role);
    }

    public Member(String email, String nickname, String password, String profileImageUrl) {
        this(email, nickname, password, profileImageUrl, Role.USER);
    }

    public Member(String email, String nickname, String password, String profileImageUrl, Role role) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        this.xp = 0;
        this.level = 1;
    }

    public void updateProfile(String nickname, String profileImageUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void addXp(int xp) {
        if (xp <= 0) {
            return;
        }
        this.xp += xp;
        this.level = (this.xp / 50) + 1;
    }
}
