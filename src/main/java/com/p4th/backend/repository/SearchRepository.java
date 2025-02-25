package com.p4th.backend.repository;

import com.p4th.backend.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SearchRepository extends JpaRepository<Post, String> {

    // 전체 검색: 제목, 내용, 그리고 작성자(회원) 닉네임으로 검색
    @Query("SELECT p FROM Post p " +
            "WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "   OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "   OR LOWER(p.user.nickname) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Post> searchPosts(@Param("query") String query, Pageable pageable);

    // 특정 게시판 내 검색: 게시판ID와 함께 제목, 내용, 작성자 닉네임으로 검색
    @Query("SELECT p FROM Post p " +
            "WHERE p.board.boardId = :boardId " +
            "  AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "    OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "    OR LOWER(p.user.nickname) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Post> searchPostsByBoard(@Param("boardId") String boardId,
                                  @Param("query") String query,
                                  Pageable pageable);
}
