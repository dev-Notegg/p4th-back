package com.p4th.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "post_attachment")
public class PostAttachment {
    @Id
    private String attachmentId;
    private String postId;
    private String fileName;
    private String fileUrl;
    private String attachType;
    private long fileSize;
    private String createdBy;
    private String updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Post post;
}
