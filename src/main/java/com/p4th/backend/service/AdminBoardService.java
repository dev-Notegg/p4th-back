package com.p4th.backend.service;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.domain.Board;
import com.p4th.backend.domain.Category;
import com.p4th.backend.dto.response.admin.BoardDeletionInfoResponse;
import com.p4th.backend.dto.response.admin.BoardResponse;
import com.p4th.backend.mapper.AdminBoardMapper;
import com.p4th.backend.repository.AdminBoardRepository;
import com.p4th.backend.util.ULIDUtil;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminBoardService {

    private final AdminBoardRepository boardRepository;
    private final AdminBoardMapper adminBoardMapper;

    @Transactional(readOnly = true)
    public Page<BoardResponse> getBoards(String boardId, String boardName, String categoryName, Pageable pageable) {
        Specification<Board> spec = (root, query, cb) -> {
            Join<Board, Category> categoryJoin = root.join("category", JoinType.INNER);
            Predicate predicate = cb.conjunction();
            if (boardId != null && !boardId.trim().isEmpty()) {
                predicate = cb.and(predicate, cb.like(root.get("boardId"), "%" + boardId + "%"));
            }
            if (boardName != null && !boardName.trim().isEmpty()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("boardName")), "%" + boardName.toLowerCase() + "%"));
            }
            if (categoryName != null && !categoryName.trim().isEmpty()) {
                predicate = cb.and(predicate, cb.like(cb.lower(categoryJoin.get("categoryName")), "%" + categoryName.toLowerCase() + "%"));
            }
            return predicate;
        };

        Page<Board> boardPage = boardRepository.findAll(spec, pageable);
        return boardPage.map(BoardResponse::from);
    }

    @Transactional
    public String createBoard(String userId, String boardName, String categoryId, int boardLevel) {
        Board existing = adminBoardMapper.findByBoardNameAndCategoryId(boardName, categoryId);
        if (existing != null) {
            throw new CustomException(ErrorCode.DUPLICATE_BOARD_NAME);
        }
        String boardId = ULIDUtil.getULID();
        Board board = new Board();
        board.setBoardId(boardId);
        board.setBoardName(boardName);
        board.setCategoryId(categoryId);
        board.setBoardLevel(boardLevel);
        board.setSortOrder(adminBoardMapper.findMaxSortOrderByCategory(categoryId) + 1);
        board.setCreatedBy(userId);
        board.setRecommendYn(0);
        adminBoardMapper.insertBoard(board);
        return boardId;
    }

    @Transactional
    public void updateBoard(String userId, String boardId, String boardName, String categoryId, int boardLevel) {
        Board board = adminBoardMapper.selectBoardById(boardId);
        if (board == null) {
            throw new CustomException(ErrorCode.BOARD_NOT_FOUND);
        }
        Board duplicate = adminBoardMapper.findByBoardNameAndCategoryId(boardName, categoryId);
        if (duplicate != null && !duplicate.getBoardId().equals(boardId)) {
            throw new CustomException(ErrorCode.DUPLICATE_BOARD_NAME);
        }
        board.setBoardName(boardName);
        board.setCategoryId(categoryId);
        board.setBoardLevel(boardLevel);
        board.setUpdatedBy(userId);
        adminBoardMapper.updateBoard(board);
    }

    @Transactional(readOnly = true)
    public BoardDeletionInfoResponse getBoardDeletionInfo(String boardId) {
        Board board = adminBoardMapper.selectBoardById(boardId);
        if (board == null) {
            throw new CustomException(ErrorCode.BOARD_NOT_FOUND);
        }
        int postCount = adminBoardMapper.countPostsByBoardId(boardId);
        int commentCount = adminBoardMapper.countCommentsByBoardId(boardId);
        return new BoardDeletionInfoResponse(board.getBoardName(), postCount, commentCount);
    }

    @Transactional
    public void deleteBoard(String boardId) {
        Board board = adminBoardMapper.selectBoardById(boardId);
        if (board == null) {
            throw new CustomException(ErrorCode.BOARD_NOT_FOUND);
        }
        adminBoardMapper.deleteBoard(boardId);
    }
}
