package com.p4th.backend.domain;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class Post {
    private String postId;
    private String boardId;
    private String userId;
    private String nickname;
    private String title;
    private String content;
    private int viewCount;
    private int commentCount;
    private String createdBy;
    private String updatedBy;

    private List<PostAttachment> attachments;
    private List<Comment> comments;
}
