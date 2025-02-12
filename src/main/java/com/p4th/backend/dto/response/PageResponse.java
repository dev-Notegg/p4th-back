package com.p4th.backend.dto.response;

import lombok.Data;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Data
public class PageResponse<T> {
    private List<T> content;
    private Pageable pageable;
    private int totalElements;
    private int totalPages;
}
