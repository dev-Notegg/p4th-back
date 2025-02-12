package com.p4th.backend.dto.response;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class CheckResponse {
    private boolean available;
    public CheckResponse() { }
}
