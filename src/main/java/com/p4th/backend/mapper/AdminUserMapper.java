package com.p4th.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminUserMapper {
    void updateMembershipLevel(@Param("userId") String userId,
                               @Param("membershipLevel") int membershipLevel);

    void updateAdminRole(@Param("userId") String userId,
                         @Param("adminRole") int adminRole);

    Integer findAdminRoleByUserId(@Param("userId") String userId);

    long countUserById(@Param("userId") String userId);
}
