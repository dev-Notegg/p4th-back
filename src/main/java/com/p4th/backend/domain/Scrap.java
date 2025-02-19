package com.p4th.backend.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Scrap {
    private String scrapId;
    private String scrapFolderId;
    private String postId;
    private LocalDateTime scrappedAt;
}
