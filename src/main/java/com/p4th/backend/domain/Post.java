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
    private String status;
    private LocalDateTime statusChangedAt;
    private int viewCount;
    private int commentCount;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    @Transient
    private List<Comment> comments;
}
