package com.p4th.backend.mapper;

import com.p4th.backend.domain.Block;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BlockMapper {
    int insertBlock(Block block);

    // 로그인한 사용자가 차단한 사용자들의 ID 목록을 조회
    List<String> findBlockedByUserId(@Param("userId") String userId);
}
