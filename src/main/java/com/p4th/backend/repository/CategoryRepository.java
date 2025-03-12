package com.p4th.backend.repository;

import com.p4th.backend.domain.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, String> {

    @Query("SELECT c FROM Category c " +
            "WHERE (:categoryId IS NULL OR LOWER(c.categoryId) LIKE LOWER(CONCAT('%', :categoryId, '%'))) " +
            "AND (:categoryName IS NULL OR LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :categoryName, '%'))) " +
            "ORDER BY c.sortOrder")
    Page<Category> searchCategories(@Param("categoryId") String categoryId,
                                    @Param("categoryName") String categoryName,
                                    Pageable pageable);
}