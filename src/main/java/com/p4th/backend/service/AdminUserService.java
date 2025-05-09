package com.p4th.backend.service;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.domain.User;
import com.p4th.backend.dto.response.user.UserProfileResponse;
import com.p4th.backend.mapper.AdminUserMapper;
import com.p4th.backend.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserMapper adminUserMapper;
    private final AdminUserRepository adminUserRepository;

    @Transactional(readOnly = true)
    public Page<UserProfileResponse> getUserList(String userId, String nickname, Pageable pageable) {
        Page<User> userPage = adminUserRepository.searchUsers(userId, nickname, pageable);
        return userPage.map(UserProfileResponse::fromWithExtraInfo);
    }

    @Transactional
    public void updateMembershipLevel(String currentUserId, String userId, int membershipLevel) {
        if (isUserNotExists(userId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        adminUserMapper.updateMembershipLevel(currentUserId, userId, membershipLevel);
    }

    @Transactional
    public void updateAdminRole(String currentUserId, String userId, int adminRole) {
        if (isUserNotExists(userId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        adminUserMapper.updateAdminRole(currentUserId, userId, adminRole);
    }

    private boolean isUserNotExists(String userId) {
        return adminUserMapper.countUserById(userId) == 0;
    }
}
