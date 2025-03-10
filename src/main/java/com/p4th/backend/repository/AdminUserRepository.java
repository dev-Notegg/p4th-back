package com.p4th.backend.repository;

import com.p4th.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AdminUserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
}
