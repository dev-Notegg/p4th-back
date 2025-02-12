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
    private String content;
    private String status;
    private LocalDateTime statusChagedAt;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    @Transient
    private String nickname;
    // 연관관계 필드 (읽기 전용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Post post;
}
