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
    private String nickname;
    private String title;
    private String content;
    private int viewCount;
    private int commentCount;
    private LocalDateTime createdAt;
    private String createdBy;
    private String updatedBy;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private List<PostAttachment> attachments;
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private List<Comment> comments;
    // 연관관계 (읽기 전용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", insertable = false, updatable = false)
    private Board board;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * 첨부파일 목록에서 첫 번째 첨부파일 URL을 반환한다.
     * 첨부파일이 없으면 null을 반환.
     */
    public String getFirstAttachmentUrl() {
        if (attachments != null && !attachments.isEmpty()) {
            return attachments.get(0).getFileUrl();
        }
        return null;
    }

    /**
     * 첨부파일 개수를 반환한다.
     */
    public int getAttachmentCount() {
        return attachments != null ? attachments.size() : 0;
    }
}
