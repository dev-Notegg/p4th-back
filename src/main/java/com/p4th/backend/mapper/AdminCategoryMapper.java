package com.p4th.backend.mapper;

import com.p4th.backend.domain.Category;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminCategoryMapper {

    // 카테고리 단건 조회
    Category findById(@Param("categoryId") String categoryId);
    
    // 카테고리 추가
    int insertCategory(Category category);
    
    // 카테고리 수정 (이름, sortOrder, mainExposure)
    int updateCategory(Category category);
    
    // 카테고리 순서 변경 (단건 업데이트)
    int updateCategoryOrder(@Param("categoryId") String categoryId,
                            @Param("sortOrder") int sortOrder);
    
    // 전체 카테고리 ID 목록 조회
    List<String> findAllCategoryIds();

    // 현재 최대 정렬 순서를 조회
    Integer getMaxSortOrder();

    // 카테고리 삭제
    int deleteCategory(@Param("categoryId") String categoryId);

    Category findByCategoryName(@Param("categoryName") String categoryName);
}
