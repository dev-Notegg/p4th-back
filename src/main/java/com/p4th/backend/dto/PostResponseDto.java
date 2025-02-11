package com.p4th.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostResponseDto {
    private String postId;
    private String boardId;
    private String userId;
    private String nickname;
    private String title;
    private String content;
    private int viewCount;
    private int commentCount;
    private LocalDateTime createdAt;
    private String createdBy;
    private List<PostAttachmentDto> attachments;
}
