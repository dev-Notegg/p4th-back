package com.p4th.backend.dto.response.admin;

import com.p4th.backend.dto.response.user.UserProfileResponse;
import lombok.Data;
import java.util.List;

@Data
public class UserListResponse {
    private long totalUsers; // 총 회원수
    private List<UserProfileResponse> users;
}
