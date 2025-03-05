package com.p4th.backend.mapper;

import com.p4th.backend.domain.Board;
import com.p4th.backend.domain.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PostMapper {
    Post getPostDetail(@Param("postId") String postId, @Param("userId") String userId);

    int insertPost(Post post);

    int updatePost(Post post);

    //실제 데이터 삭제
    int physicalDeletePost(@Param("postId") String postId);

    List<Post> getAllPosts();

    int incrementViewCount(String postId);

    void incrementCommentCount(String postId);

    List<Post> findRecentPostsByUserId(@Param("userId") String userId);

    int insertPostView(@Param("userId") String userId, @Param("postId") String postId);

    Board getBoardWithCategory(@Param("boardId") String boardId);

    int updateCommentCount(@Param("postId") String postId, @Param("count") int count);

    List<String> getDistinctUserIdsFromPostView();

    LocalDateTime get16thLatestViewedAt(@Param("userId") String userId);

    int deletePostViewsOlderThan(@Param("userId") String userId, @Param("cutoff") LocalDateTime cutoff);
}
