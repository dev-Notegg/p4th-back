package com.p4th.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "comment")
public class Comment {
    @Id
    private String commentId;
    @Column(name = "post_id")
    private String postId;
    private String parentCommentId;
    private String userId;
    @Transient
    private String nickname;
    @Transient
    private String targetNickname; // 대댓글의 댓글 대상 닉네임 (대대댓글인 경우)
    @Transient
    private String grandParentCommentId; // 부모의 부모 ID
    private String content;
    @Enumerated(EnumType.STRING)
    private CommentStatus status;  // NORMAL, REPORTED
    private LocalDateTime statusChangedAt;
    private Boolean secretYn;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    // 연관관계 필드 (읽기 전용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Post post;
}
