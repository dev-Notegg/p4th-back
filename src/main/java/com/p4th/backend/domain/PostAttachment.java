package com.p4th.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "post_attachment")
public class PostAttachment {
    @Id
    private String attachmentId;
    @Column(name = "post_id")
    private String postId;
    private String fileName;
    private String fileUrl;
    private String attachType;
    private long fileSize;
    private String createdBy;
    private LocalDateTime createdAt;
    // 연관관계 (읽기 전용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Post post;
}
