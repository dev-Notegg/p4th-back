package com.p4th.backend.service;

import com.p4th.backend.dto.SearchResponse;
import com.p4th.backend.mapper.SearchMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchMapper searchMapper;

    public SearchResponse search(String query) {
        SearchResponse response = new SearchResponse();
        // 와일드카드를 포함하여 검색 (MyBatis에서 LIKE 사용)
        response.setResults(searchMapper.searchPosts("%" + query + "%"));
        response.setTotal(response.getResults().size());
        return response;
    }
}
