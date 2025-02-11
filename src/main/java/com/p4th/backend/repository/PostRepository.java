package com.p4th.backend.repository;

import com.p4th.backend.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, String> {
    Page<Post> findByBoardId(String boardId, Pageable pageable);
}
