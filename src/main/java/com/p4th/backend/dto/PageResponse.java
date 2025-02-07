package com.p4th.backend.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class PageResponse<T> {
    private List<T> content;
    private Pageable pageable;
    private int totalElements;
    private int totalPages;

    @Getter
    @Setter
    public static class Pageable {
        private int pageNumber;
        private int pageSize;
    }
}
