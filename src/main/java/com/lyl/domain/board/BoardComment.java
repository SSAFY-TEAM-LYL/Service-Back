package com.lyl.domain.board;

import com.lyl.domain.common.BaseEntity;
import com.lyl.domain.member.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "board_comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_post_id", nullable = false)
    private BoardPost post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member author;

    public BoardComment(String content, BoardPost post, Member author) {
        this.content = content;
        this.post = post;
        this.author = author;
    }

    public void update(String content) {
        this.content = content;
    }

    public boolean isWrittenBy(Long memberId) {
        return author.getId().equals(memberId);
    }
}
