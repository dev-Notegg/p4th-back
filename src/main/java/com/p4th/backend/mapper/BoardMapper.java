package com.p4th.backend.mapper;

import com.p4th.backend.dto.PopularBoardResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface BoardMapper {
    @Select("SELECT b.board_id AS boardId, " +
            "       c.category_name AS category, " +
            "       b.board_name AS boardName " +
            "FROM board b " +
            "LEFT JOIN category c ON b.category_id = c.category_id " +
            "WHERE b.status = 'NORMAL' " +
            "ORDER BY (SELECT COUNT(*) FROM post p WHERE p.board_id = b.board_id) DESC " +
            "LIMIT 7")
    List<PopularBoardResponse> getPopularBoards();
}
