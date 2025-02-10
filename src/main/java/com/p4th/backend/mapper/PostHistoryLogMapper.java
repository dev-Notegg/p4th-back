package com.p4th.backend.mapper;

import com.p4th.backend.dto.PopularPostResponse;
import com.p4th.backend.domain.PostHistoryLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface PostHistoryLogMapper {

    @Insert("INSERT INTO post_history_log (history_id, post_id, period_type, period_start_date, period_end_date, view_count, comment_count, popularity_score, created_by) " +
            "VALUES (#{historyId}, #{postId}, #{periodType}, #{periodStartDate}, #{periodEndDate}, #{viewCount}, #{commentCount}, #{popularityScore}, #{createdBy})")
    void insertHistoryLog(PostHistoryLog log);

    @Select("SELECT p.post_id AS postId, p.board_id AS boardId, p.user_id AS userId, p.title AS title, " +
            "       c.category_name AS category, b.board_name AS boardName, h.view_count AS viewCount, h.comment_count AS commentCount, " +
            "       (SELECT pa.file_url FROM post_attachment pa WHERE pa.post_id = p.post_id LIMIT 1) AS imageUrl, " +
            "       (SELECT COUNT(*) FROM post_attachment pa WHERE pa.post_id = p.post_id) AS imageCount, " +
            "       p.created_at AS createdAt " +
            "FROM post_history_log h " +
            "JOIN post p ON h.post_id = p.post_id " +
            "JOIN board b ON p.board_id = b.board_id " +
            "JOIN category c ON b.category_id = c.category_id " +
            "WHERE h.period_type = #{period} " +
            "ORDER BY h.popularity_score DESC " +
            "LIMIT 20")
    List<PopularPostResponse> getPopularPostsByPeriod(@Param("period") String period);
}
