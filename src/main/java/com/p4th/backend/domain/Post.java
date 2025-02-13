package com.p4th.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "post")
@Getter
@Setter
public class Post {
    @Id
    private String postId;
    @Column(name = "board_id")
    private String boardId;
    @Column(name = "user_id")
    private String userId;
    @Transient
    private String nickname;
    private String title;
    private String content;
    private int pinnedYn;
    @Enumerated(EnumType.STRING)
    private PostStatus status;  // NORMAL, REPORTED, DELETED
    private LocalDateTime statusChangedAt;
    private int viewCount;
    private int commentCount;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private List<Comment> comments;
    // 연관관계 (읽기 전용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", insertable = false, updatable = false)
    private Board board;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}
