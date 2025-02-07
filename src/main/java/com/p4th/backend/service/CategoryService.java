package com.p4th.backend.service;

import com.p4th.backend.domain.Board;
import com.p4th.backend.domain.Category;
import com.p4th.backend.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public List<Category> getAllCategories() {
        return categoryMapper.getAllCategories();
    }

    public Category getBoardsByCategory(String categoryId) {
        Category category = categoryMapper.getCategoryById(categoryId);
        List<Board> boards = categoryMapper.getBoardsByCategory(categoryId);
        category.setBoards(boards);
        return category;
    }
}
