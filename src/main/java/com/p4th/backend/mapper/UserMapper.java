package com.p4th.backend.mapper;

import com.p4th.backend.domain.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    void insertUser(User user);

    User selectByLoginId(String loginId);

    void updateTokens(User user);

    void updateLastLoginInfo(User user);
}
