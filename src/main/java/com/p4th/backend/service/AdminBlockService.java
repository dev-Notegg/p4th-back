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
    public Page<BlockUserResponse> getBlockList(boolean blockedOnly, String ip, String userId, String nickname, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            Predicate p = cb.conjunction();
            if (ip != null && !ip.trim().isEmpty()) {
                p = cb.and(p, cb.like(root.get("lastLoginIp"), "%" + ip + "%"));
            }
            if (userId != null && !userId.trim().isEmpty()) {
                p = cb.and(p, cb.like(root.get("userId"), "%" + userId + "%"));
            }
            if (nickname != null && !nickname.trim().isEmpty()) {
                p = cb.and(p, cb.like(cb.lower(root.get("nickname")), "%" + nickname.toLowerCase() + "%"));
            }
            if (blockedOnly) {
                p = cb.and(p, cb.equal(root.get("accountStatus"), AccountStatus.BLOCKED));
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
        if (user.getAccountStatus() == AccountStatus.BLOCKED) {
            throw new CustomException(ErrorCode.ALREADY_PROCESSED, "해당 회원은 이미 차단된 상태입니다.");
        }
        user.setAccountStatus(AccountStatus.BLOCKED);
        user.setAccountStatusChangedAt(LocalDateTime.now());
        adminUserRepository.save(user);

        // IP 차단 테이블 업데이트
        String ip = user.getLastLoginIp();
        if (ip != null && !ip.isEmpty()) {
            IpBlacklist existing = ipBlacklistMapper.findByIpAddress(ip);
            if (existing != null) {
                existing.setStatus("BLOCKED");
                existing.setUpdatedBy(currentUserId);
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
        // 이미 활성 상태이면 예외 발생
        if (user.getAccountStatus() == AccountStatus.ACTIVE) {
            throw new CustomException(ErrorCode.ALREADY_PROCESSED, "해당 회원은 이미 활성 상태입니다.");
        }
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
        // 해당 IP의 차단 상태를 조회
        return ipBlacklistMapper.findByIpAddress(ip) != null &&
                "BLOCKED".equals(ipBlacklistMapper.findByIpAddress(ip).getStatus());
    }
}
