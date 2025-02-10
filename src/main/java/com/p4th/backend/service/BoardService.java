package com.p4th.backend.service;

import com.p4th.backend.dto.PopularBoardResponse;
import com.p4th.backend.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardMapper boardMapper;

    public List<PopularBoardResponse> getPopularBoards() {
        return boardMapper.getPopularBoards();
    }
}
