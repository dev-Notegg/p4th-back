package com.p4th.backend.mapper;

import com.p4th.backend.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthMapper {
    void insertUser(User user);
    void updateTokens(User user);
    void updateLastLoginInfo(User user);
    User selectByPassCode(@Param("passCode") String passCode);
    User selectByUserId(@Param("userId") String userId);
    void updatePassword(User user);
    User selectByNickname(@Param("nickname") String nickname);
    void updateUserNickname(User user);
    void deleteUser(@Param("userId") String userId);
}
