package com.p4th.backend.service;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.domain.Board;
import com.p4th.backend.domain.Category;
import com.p4th.backend.dto.request.BoardOrderUpdateRequest;
import com.p4th.backend.dto.response.admin.BoardListResponse;
import com.p4th.backend.dto.response.admin.BoardResponse;
import com.p4th.backend.dto.response.admin.CategoryResponse;
import com.p4th.backend.mapper.AdminCategoryMapper;
import com.p4th.backend.mapper.AdminBoardMapper;
import com.p4th.backend.repository.CategoryRepository;
import com.p4th.backend.util.ULIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCategoryService {

    private final AdminCategoryMapper adminCategoryMapper;
    private final AdminBoardMapper adminBoardMapper;

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<CategoryResponse> getCategories(String categoryId, String categoryName, Pageable pageable) {
        Page<Category> categoryPage;
        if ((categoryId != null && !categoryId.trim().isEmpty()) ||
                (categoryName != null && !categoryName.trim().isEmpty())) {
            categoryPage = categoryRepository.searchCategories(categoryId, categoryName, pageable);
        } else {
            categoryPage = categoryRepository.findAll(pageable);
        }
        return categoryPage.map(CategoryResponse::from);
    }

    @Transactional
    public void updateMainExposure(String categoryId, int mainExposure) {
        Category category = adminCategoryMapper.findById(categoryId);
        if(category == null){
            throw new CustomException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        category.setMainExposure(mainExposure);
        adminCategoryMapper.updateCategory(category);
    }

    @Transactional
    public String createCategory(String categoryName) {
        Category existingCategory = adminCategoryMapper.findByCategoryName(categoryName);
        if (existingCategory != null) {
            throw new CustomException(ErrorCode.DUPLICATE_CATEGORY_NAME);
        }

        Category category = new Category();
        category.setCategoryId(ULIDUtil.getULID());
        category.setCategoryName(categoryName);
        // 현재 최대 sortOrder를 조회 (없으면 0)
        Integer maxSortOrder = adminCategoryMapper.getMaxSortOrder();
        if(maxSortOrder == null) {
            maxSortOrder = 0;
        }
        category.setSortOrder(maxSortOrder + 1);
        adminCategoryMapper.insertCategory(category);
        return category.getCategoryId();
    }

    @Transactional
    public void updateCategoryOrder(List<String> order) {
        List<String> allCategoryIds = adminCategoryMapper.findAllCategoryIds();
        if (order.size() != allCategoryIds.size()) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "모든 카테고리의 순서를 지정해주세요.");
        }
        if (!new HashSet<>(order).containsAll(allCategoryIds)) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "요청된 카테고리 목록이 올바르지 않습니다.");
        }
        for (int i = 0; i < order.size(); i++) {
            String categoryId = order.get(i);
            adminCategoryMapper.updateCategoryOrder(categoryId, i);
        }
    }

    @Transactional(readOnly = true)
    public BoardListResponse getBoardsByCategory(String categoryId) {
        List<Board> boards = adminBoardMapper.findByCategoryIdOrderBySortOrder(categoryId);
        List<BoardResponse> boardResponses = boards.stream()
                .map(BoardResponse::from)
                .collect(Collectors.toList());
        return new BoardListResponse(boardResponses);
    }

    @Transactional
    public void updateBoardOrder(String categoryId, BoardOrderUpdateRequest requestDto) {
        // 만약 sortType이 postCount이면 게시글 수 기준 자동 정렬
        if ("postCount".equalsIgnoreCase(requestDto.getSortType())) {
            List<String> sortedBoardIds = adminBoardMapper.findBoardIdsByCategorySortedByPostCount(categoryId);
            for (int i = 0; i < sortedBoardIds.size(); i++) {
                adminBoardMapper.updateBoardOrder(categoryId, sortedBoardIds.get(i), i);
            }
        } else { // 수동 정렬
            List<String> order = requestDto.getOrder();
            for (int i = 0; i < order.size(); i++) {
                adminBoardMapper.updateBoardOrder(categoryId, order.get(i), i);
            }
        }
    }

    /**
     * 카테고리명 수정
     * @param categoryId 수정할 카테고리의 ID
     * @param newCategoryName 새로 적용할 카테고리명
     */
    @Transactional
    public void updateCategoryName(String categoryId, String newCategoryName) {
        Category category = adminCategoryMapper.findById(categoryId);
        if (category == null) {
            throw new CustomException(ErrorCode.CATEGORY_NOT_FOUND);

        }
        Category existingCategory = adminCategoryMapper.findByCategoryName(newCategoryName);
        if (existingCategory != null) {
            throw new CustomException(ErrorCode.DUPLICATE_CATEGORY_NAME);
        }
        category.setCategoryName(newCategoryName);
        int updated = adminCategoryMapper.updateCategory(category);
        if (updated != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "카테고리명 수정에 실패하였습니다.");
        }
    }

    /**
     * 카테고리 삭제
     * @param categoryId 삭제할 카테고리의 ID
     */
    @Transactional
    public void deleteCategory(String categoryId) {
        Category category = adminCategoryMapper.findById(categoryId);
        if (category == null) {
            throw new CustomException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        int deleted = adminCategoryMapper.deleteCategory(categoryId);
        if (deleted != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "카테고리 삭제에 실패하였습니다.");
        }
    }
}
