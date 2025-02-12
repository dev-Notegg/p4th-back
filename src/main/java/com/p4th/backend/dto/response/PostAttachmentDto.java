package com.p4th.backend.dto.response;

import lombok.Data;

@Data
public class PostAttachmentDto {
    private String attachmentId;
    private String postId;
    private String fileName;
    private String fileUrl;
    private String attachType;
    private long fileSize;
}
