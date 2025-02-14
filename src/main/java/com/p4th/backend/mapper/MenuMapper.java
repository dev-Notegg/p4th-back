package com.p4th.backend.mapper;

import com.p4th.backend.domain.Board;
import com.p4th.backend.domain.Category;
import com.p4th.backend.domain.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuMapper {
    List<Comment> getCommentsByUser(@Param("userId") String userId);
    List<Category> getAllCategories();
    List<Board> getBoardsByCategory(@Param("categoryId") String categoryId);
}
