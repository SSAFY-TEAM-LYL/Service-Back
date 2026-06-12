package com.lyl.domain.board;

import com.lyl.domain.common.BaseEntity;
import com.lyl.domain.member.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "board_posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardPost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BoardCategory category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member author;

    @Column(nullable = false)
    private long viewCount;

    @Column(nullable = false)
    private int commentCount;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardComment> comments = new ArrayList<>();

    public BoardPost(String title, String content, Member author) {
        this(title, content, BoardCategory.FREE, author);
    }

    public BoardPost(String title, String content, BoardCategory category, Member author) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.author = author;
        this.viewCount = 0;
        this.commentCount = 0;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void update(String title, String content, BoardCategory category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }

    public boolean isWrittenBy(Long memberId) {
        return author.getId().equals(memberId);
    }

    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void decreaseCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }
}
