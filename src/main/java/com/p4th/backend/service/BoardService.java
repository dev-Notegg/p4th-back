package com.p4th.backend.service;

import com.p4th.backend.domain.Board;
import com.p4th.backend.dto.BoardResponseDto;
import com.p4th.backend.dto.PopularBoardResponse;
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
        // DTO로 변환하여 categoryName 필드는 categoryMapper를 통해 가져오거나, 또는 Category 엔티티에서 직접 가져올 수 있다.
        // 예를 들어, 아래와 같이 변환합니다.
        return boards.stream().map(board -> {
            BoardResponseDto dto = new BoardResponseDto();
            dto.setBoardId(board.getBoardId());
            dto.setCategoryId(board.getCategoryId());
            dto.setBoardName(board.getBoardName());
            dto.setBoardLevel(board.getBoardLevel());
            dto.setSortOrder(board.getSortOrder());
            dto.setRecommend_yn(board.getRecommend_yn());
            dto.setStatus(board.getStatus());
            dto.setStatusChangedAt(board.getStatusChangedAt());
            dto.setCreatedBy(board.getCreatedBy());
            dto.setCreatedAt(board.getCreatedAt());
            dto.setUpdatedBy(board.getUpdatedBy());
            dto.setUpdatedAt(board.getUpdatedAt());
            // categoryName는 별도로 조회하여 설정할 수 있습니다.
            // 예를 들어, 만약 Category 엔티티가 board.getCategory()로 가져올 수 있다면:
            if (board.getCategory() != null) {
                dto.setCategoryName(board.getCategory().getCategoryName());
            }
            return dto;
        }).collect(Collectors.toList());
    }
}
