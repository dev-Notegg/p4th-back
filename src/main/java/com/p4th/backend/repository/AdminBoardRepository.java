package com.p4th.backend.repository;

import com.p4th.backend.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AdminBoardRepository extends JpaRepository<Board, String>, JpaSpecificationExecutor<Board> {

}
