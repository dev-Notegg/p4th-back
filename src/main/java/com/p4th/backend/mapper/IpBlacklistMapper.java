package com.p4th.backend.mapper;

import com.p4th.backend.domain.IpBlacklist;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IpBlacklistMapper {
    IpBlacklist findByIpAddress(String ipAddress);
    void updateIpBlacklist(IpBlacklist ipBlacklist);
    void insertIpBlacklist(IpBlacklist ipBlacklist);
    void deleteByIpAddress(String ipAddress);
}
