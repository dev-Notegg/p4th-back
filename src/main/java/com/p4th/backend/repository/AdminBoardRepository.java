package com.p4th.backend.repository;

import com.p4th.backend.domain.Board;
import com.p4th.backend.dto.response.admin.BoardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminBoardRepository extends JpaRepository<Board, String> {

    @Query("SELECT new com.p4th.backend.dto.response.admin.BoardResponse(" +
            "b.boardId, b.boardName, b.categoryId, c.categoryName, " +
            "b.boardLevel, b.recommendYn, b.sortOrder, b.createdAt, b.updatedAt) " +
            "FROM Board b JOIN Category c ON b.categoryId = c.categoryId " +
            "WHERE (:boardId IS NULL OR b.boardId LIKE CONCAT('%', :boardId, '%')) " +
            "AND (:boardName IS NULL OR LOWER(b.boardName) LIKE LOWER(CONCAT('%', :boardName, '%'))) " +
            "AND (:categoryName IS NULL OR LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :categoryName, '%')))")
    Page<BoardResponse> searchBoards(@Param("boardId") String boardId,
                                     @Param("boardName") String boardName,
                                     @Param("categoryName") String categoryName,
                                     Pageable pageable);

    @Query("SELECT COALESCE(MAX(b.sortOrder), 0) FROM Board b WHERE b.categoryId = :categoryId")
    int findMaxSortOrderByCategory(@Param("categoryId") String categoryId);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.boardId = :boardId")
    int countByBoardId(@Param("boardId") String boardId);

    // 댓글 수 조회 (게시판 내 모든 게시글의 댓글 수 합계)
    @Query("SELECT COALESCE(SUM(p.commentCount), 0) FROM Post p WHERE p.boardId = :boardId")
    int countCommentsByBoardId(@Param("boardId") String boardId);

    Board findByBoardNameAndCategoryId(String boardName, String categoryId);
}
