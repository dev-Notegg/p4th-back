package com.p4th.backend.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostAttachment {
    private String attachmentId;
    private String postId;
    private String fileName;
    private String fileUrl;
    private String attachType;
    private long fileSize;
    private String createdBy;
    private String updatedBy;
}
