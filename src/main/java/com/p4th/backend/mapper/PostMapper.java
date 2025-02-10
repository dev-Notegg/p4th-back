package com.p4th.backend.mapper;

import com.p4th.backend.domain.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PostMapper {
    List<Post> getPostsByBoard(@Param("boardId") String boardId,
                               @Param("size") int size,
                               @Param("offset") int offset);

    int countPostsByBoard(@Param("boardId") String boardId);

    Post getPostDetail(@Param("postId") String postId);

    int insertPost(Post post);

    int updatePost(Post post);

    int deletePost(@Param("postId") String postId);

    List<Post> getAllPosts();

    void incrementViewCount(String postId);
}
