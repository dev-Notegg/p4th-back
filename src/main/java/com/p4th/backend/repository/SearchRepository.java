package com.p4th.backend.repository;

import com.p4th.backend.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchRepository extends JpaRepository<Post, String> {

    // 전체 검색: 제목, 내용, 그리고 작성자(회원) 닉네임으로 검색
    Page<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrUser_NicknameContainingIgnoreCase(
            String title, String content, String nickname, Pageable pageable);

    // 특정 게시판 내 검색: 게시판ID와 함께 제목, 내용, 작성자 닉네임으로 검색
    Page<Post> findByBoard_BoardIdAndTitleContainingIgnoreCaseOrBoard_BoardIdAndContentContainingIgnoreCaseOrBoard_BoardIdAndUser_NicknameContainingIgnoreCase(
            String boardId1, String title,
            String boardId2, String content,
            String boardId3, String nickname,
            Pageable pageable);
}
