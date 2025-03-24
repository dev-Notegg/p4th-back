package com.p4th.backend.service;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.domain.AccountStatus;
import com.p4th.backend.domain.IpBlacklist;
import com.p4th.backend.domain.User;
import com.p4th.backend.dto.response.admin.BlockUserResponse;
import com.p4th.backend.mapper.IpBlacklistMapper;
import com.p4th.backend.repository.AdminUserRepository;
import com.p4th.backend.util.ULIDUtil;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminBlockService {

    private final AdminUserRepository adminUserRepository;
    private final IpBlacklistMapper ipBlacklistMapper;

    @Transactional(readOnly = true)
    public Page<BlockUserResponse> getBlockList(String ip, String userId, String nickname, boolean blockedOnly, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            Predicate p = cb.conjunction();
            if (ip != null && !ip.trim().isEmpty()) {
                p = cb.and(p, cb.equal(root.get("lastLoginIp"), ip));
            }
            if (userId != null && !userId.trim().isEmpty()) {
                p = cb.and(p, cb.like(root.get("userId"), "%" + userId + "%"));
            }
            if (nickname != null && !nickname.trim().isEmpty()) {
                p = cb.and(p, cb.like(cb.lower(root.get("nickname")), "%" + nickname.toLowerCase() + "%"));
            }
            if (blockedOnly) {
                p = cb.and(p, cb.equal(root.get("accountStatus"), "BLOCKED"));
            }
            return p;
        };
        Page<User> users = adminUserRepository.findAll(spec, pageable);
        return users.map(BlockUserResponse::from);
    }

    @Transactional
    public void blockUser(String userId, String currentUserId) {
        User user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.setAccountStatus(AccountStatus.BLOCKED);
        user.setAccountStatusChangedAt(LocalDateTime.now());
        adminUserRepository.save(user);

        // IP 차단 테이블 업데이트
        String ip = user.getLastLoginIp();
        if (ip != null && !ip.isEmpty()) {
            IpBlacklist existing = ipBlacklistMapper.findByIpAddress(ip);
            if (existing != null) {
                existing.setStatus("BLOCKED");
                existing.setUpdatedAt(LocalDateTime.now());
                existing.setUpdatedBy(currentUserId); // 필요시 현재 관리자로 변경
                ipBlacklistMapper.updateIpBlacklist(existing);
            } else {
                IpBlacklist newRecord = new IpBlacklist();
                newRecord.setBlacklistId(ULIDUtil.getULID());
                newRecord.setIpAddress(ip);
                newRecord.setStatus("READY"); // 신규 IP는 우선 READY 상태로 저장
                newRecord.setCreatedBy(currentUserId);
                ipBlacklistMapper.insertIpBlacklist(newRecord);
            }
        }
    }

    @Transactional
    public void unblockUser(String userId, String currentUserId) {
        User user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setAccountStatusChangedAt(LocalDateTime.now());
        user.setUpdatedBy(currentUserId);
        adminUserRepository.save(user);

        String ip = user.getLastLoginIp();
        if (ip != null && !ip.isEmpty()) {
            ipBlacklistMapper.deleteByIpAddress(ip);
        }
    }

    public boolean isIpBlocked(String ip) {
        // ipBlacklistMapper를 통해 해당 IP의 차단 상태를 조회
        return ipBlacklistMapper.findByIpAddress(ip) != null &&
                "BLOCKED".equals(ipBlacklistMapper.findByIpAddress(ip).getStatus());
    }
}
