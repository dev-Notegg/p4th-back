package com.p4th.backend.mapper;

import com.p4th.backend.domain.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CategoryMapper {
    List<Category> getAllCategories();
    Category getCategoryById(@Param("categoryId") String categoryId);
}
