package com.p4th.backend.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Comment {
    private String commentId;
    private String postId;
    private String parentCommentId;
    private String userId;
    private String content;
    private String loginId;
    private String createdBy;
    private String updatedBy;
}
