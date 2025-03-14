package com.p4th.backend.repository;

import com.p4th.backend.domain.Post;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, String> {
    Page<Post> findByBoardId(String boardId, Pageable pageable);

    Page<Post> findByUserId(String userId, Pageable pageable);

    // 로그인한 사용자의 차단 조건을 적용한 게시글 조회
    @Query("select p from Post p " +
            "where p.boardId = :boardId " +
            "and (:userId is null or p.userId not in (" +
            "    select b.targetUserId from Block b where b.userId = :userId))")
    Page<Post> findByBoardIdExcludingBlocked(@Param("boardId") String boardId,
                                             @Param("userId") String userId,
                                             Pageable pageable);

    // 내가 작성한 댓글이 포함된 게시글을 조회 (중복 게시글 제거를 위해 DISTINCT 사용)
    @Query("SELECT DISTINCT p FROM Post p JOIN p.comments c WHERE c.userId = :userId ORDER BY p.createdAt DESC")
    Page<Post> findPostsWithUserComments(@Param("userId") String userId, Pageable pageable);
}
