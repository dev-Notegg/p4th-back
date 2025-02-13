package com.p4th.backend.dto.response.auth;

import lombok.Data;

@Data
public class FindPasswordResponse {
    private String password;
    public FindPasswordResponse(String password) {
        this.password = password;
    }
}
