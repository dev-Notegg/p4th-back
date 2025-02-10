package com.p4th.backend.mapper;

import com.p4th.backend.domain.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    void insertUser(User user);
    void updateTokens(User user);
    void updateLastLoginInfo(User user);
    User selectByPassCode(String passCode);
    User selectByUserId(String userId);
    void updatePassword(User user);
    User selectByNickname(String nickname);
}
