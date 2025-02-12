package com.p4th.backend.dto.response;

import com.p4th.backend.domain.Post;
import lombok.Data;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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
    private String createdAt;
    private String createdBy;
    private List<PostAttachmentDto> attachments;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static PostResponseDto from(Post post) {
        PostResponseDto dto = new PostResponseDto();
        dto.postId = post.getPostId();
        dto.boardId = post.getBoardId();
        dto.userId = post.getUserId();
        dto.nickname = post.getUser() != null ? post.getUser().getNickname() : "";
        dto.title = post.getTitle();
        dto.content = post.getContent();
        dto.viewCount = post.getViewCount();
        dto.commentCount = post.getCommentCount();
        dto.createdAt = post.getCreatedAt() != null ? post.getCreatedAt().format(formatter) : null;
        dto.createdBy = post.getCreatedBy();
        if (post.getAttachments() != null) {
            dto.attachments = post.getAttachments().stream().map(attachment -> {
                PostAttachmentDto attDto = new PostAttachmentDto();
                attDto.setAttachmentId(attachment.getAttachmentId());
                attDto.setPostId(attachment.getPostId());
                attDto.setFileName(attachment.getFileName());
                attDto.setFileUrl(attachment.getFileUrl());
                attDto.setAttachType(attachment.getAttachType());
                attDto.setFileSize(attachment.getFileSize());
                return attDto;
            }).collect(Collectors.toList());
        }
        return dto;
    }
}
