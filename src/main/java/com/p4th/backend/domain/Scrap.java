package com.p4th.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "scrap")
@Getter
@Setter
public class Scrap {
    @Id
    private String scrapId;
    private String userId;
    @Column(name = "scrap_folder_id")
    private String scrapFolderId;
    @Column(name = "post_id")
    private String postId;
    private LocalDateTime scrappedAt;
    // 연관관계: Scrap -> Post (읽기 전용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Post post;
}
