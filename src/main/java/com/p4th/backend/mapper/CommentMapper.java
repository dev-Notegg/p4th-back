package com.p4th.backend.mapper;

import com.p4th.backend.domain.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CommentMapper {
    List<Comment> getCommentsByPost(@Param("postId") String postId);

    int insertComment(Comment comment);

    int updateComment(@Param("commentId") String commentId, @Param("content") String content);

    int deleteComment(@Param("commentId") String commentId);

    Comment getCommentById(@Param("commentId") String commentId);
}
