package com.p4th.backend.mapper;

import com.p4th.backend.domain.Board;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminBoardMapper {
    
    // 카테고리 내 게시판 조회
    List<Board> findByCategoryIdOrderBySortOrder(@Param("categoryId") String categoryId);
    
    // 게시판 순서 변경 (단건 업데이트)
    int updateBoardOrder(@Param("userId") String userId,
                         @Param("categoryId") String categoryId,
                         @Param("boardId") String boardId,
                         @Param("sortOrder") int sortOrder);

    List<String> findBoardIdsByCategorySortedByPostCount(@Param("categoryId") String categoryId);

    int insertBoard(Board board);

    int updateBoard(Board board);

    Board selectBoardById(@Param("boardId") String boardId);

    int deleteBoard(@Param("boardId") String boardId);

    int findMaxSortOrderByCategory(@Param("categoryId") String categoryId);

    int countPostsByBoardId(@Param("boardId") String boardId);

    int countCommentsByBoardId(@Param("boardId") String boardId);

    Board findByBoardNameAndCategoryId(@Param("boardName") String boardName,
                                       @Param("categoryId") String categoryId);
}
