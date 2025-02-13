package com.p4th.backend.mapper;

import com.p4th.backend.domain.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PostMapper {
    int countPostsByBoard(@Param("boardId") String boardId);

    Post getPostDetail(@Param("postId") String postId);

    int insertPost(Post post);

    int updatePost(Post post);

    //상태값 변경
    int deletePost(@Param("postId") String postId);

    //실제 데이터 삭제
    int physicalDeletePost(@Param("postId") String postId);

    List<Post> getAllPosts();

    void incrementViewCount(String postId);

    void incrementCommentCount(String postId);

    void decrementCommentCount(String postId);

    /**
     * 지정한 사용자(userId)가 최근 본 게시물 목록을 offset, limit에 따라 조회한다.
     *
     * @param userId 조회할 사용자 ID
     * @return 조회된 게시글 목록
     */
    List<Post> findRecentPostsByUserId(@Param("userId") String userId);
}
