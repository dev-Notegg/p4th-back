package com.p4th.backend.repository;

import com.p4th.backend.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminUserRepository extends JpaRepository<User, String> {

    @Query("SELECT u FROM User u " +
            "WHERE (:userId IS NULL OR LOWER(u.userId) LIKE LOWER(CONCAT('%', :userId, '%'))) " +
            "AND (:nickname IS NULL OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :nickname, '%')))")
    Page<User> searchUsers(@Param("userId") String userId,
                           @Param("nickname") String nickname,
                           Pageable pageable);
}
