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
    @Column(name = "attachment_id")
    private String attachmentId;
    private String postId;
    private String fileName;
    private String fileUrl;
    private String attachType;
    private long fileSize;
    private String createdBy;
    private String updatedBy;
}
