package com.p4th.backend.service;

import com.p4th.backend.domain.Board;
import com.p4th.backend.dto.response.BoardResponseDto;
import com.p4th.backend.dto.response.PopularBoardResponse;
import com.p4th.backend.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardMapper boardMapper;

    public List<PopularBoardResponse> getPopularBoards() {
        return boardMapper.getPopularBoards();
    }

    public List<BoardResponseDto> getBoardsByCategory(String categoryId) {
        List<Board> boards = boardMapper.getBoardsByCategory(categoryId);
        return boards.stream().map(board -> {
            BoardResponseDto dto = new BoardResponseDto();
            // 필드 복사 (getter/setter 활용)
            dto.setBoardId(board.getBoardId());
            dto.setCategoryId(board.getCategoryId());
            dto.setCategoryName(board.getCategoryName());
            dto.setBoardName(board.getBoardName());
            dto.setBoardLevel(board.getBoardLevel());
            dto.setSortOrder(board.getSortOrder());
            dto.setRecommendYn(board.getRecommendYn());
            dto.setCreatedBy(board.getCreatedBy());
            dto.setCreatedAt(board.getCreatedAt());
            dto.setUpdatedBy(board.getUpdatedBy());
            dto.setUpdatedAt(board.getUpdatedAt());
            return dto;
        }).collect(Collectors.toList());
    }
}
