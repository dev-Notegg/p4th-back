package com.p4th.backend.common;

import com.p4th.backend.util.ULIDUtil;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Builder
public class CommonResponse {
    @Builder.Default
    private String id = ULIDUtil.getULID();
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    private Object data;
}