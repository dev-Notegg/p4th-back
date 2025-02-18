package com.p4th.backend.mapper;

import com.p4th.backend.domain.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CommentMapper {
    List<Comment> getCommentsByPost(@Param("postId") String postId);

    int insertComment(Comment comment);

    int updateComment(@Param("commentId") String commentId, @Param("content") String content, @Param("userId") String userId);
    //상태값 업데이트
    int deleteComment(@Param("commentId") String commentId, @Param("userId") String userId);
    //실제 데이터 삭제
    int physicalDeleteComment(@Param("commentId") String commentId);
    // 자식 댓글 개수 조회
    int countChildComments(@Param("commentId") String commentId);
    Comment getCommentById(@Param("commentId") String commentId);
}
