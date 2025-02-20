package com.p4th.backend.dto.response.scrap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.p4th.backend.domain.ScrapFolder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "스크랩 폴더 응답 DTO")
public class ScrapFolderResponse {
    @Schema(description = "스크랩 폴더 ID", example = "01JKQ90E6N6FP7YRWCVVC52KW0")
    private String scrapFolderId;

    @Schema(description = "폴더 이름", example = "사주")
    private String folderName;

    @Schema(description = "정렬 순서", example = "0")
    private int sortOrder;

    @Schema(description = "해당 폴더에 속한 스크랩 게시글 개수", example = "3")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int scrapCount;

    public static ScrapFolderResponse from(ScrapFolder folder) {
        ScrapFolderResponse response = new ScrapFolderResponse();
        response.setScrapFolderId(folder.getScrapFolderId());
        response.setFolderName(folder.getFolderName());
        response.setSortOrder(folder.getSortOrder());
        return response;
    }

    public static ScrapFolderResponse fromWithCount(ScrapFolder folder, int scrapCount) {
        ScrapFolderResponse response = from(folder);
        response.setScrapCount(scrapCount);
        return response;
    }
}
