package com.p4th.backend.service;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.domain.Block;
import com.p4th.backend.mapper.BlockMapper;
import com.p4th.backend.util.ULIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BlockService {
    private final BlockMapper blockMapper;

    @Transactional
    public String blockUser(String userId, String targetUserId) {
        Block block = new Block();
        block.setBlockId(ULIDUtil.getULID());
        block.setUserId(userId);
        block.setTargetUserId(targetUserId);
        int inserted = blockMapper.insertBlock(block);
        if (inserted != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "사용자 차단 실패");
        }
        return block.getBlockId();
    }
}
