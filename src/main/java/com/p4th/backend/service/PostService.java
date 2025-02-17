package com.p4th.backend.service;

import com.p4th.backend.domain.Post;
import com.p4th.backend.domain.PostStatus;
import com.p4th.backend.domain.User;
import com.p4th.backend.dto.response.post.PostListResponse;
import com.p4th.backend.mapper.*;
import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.repository.PostRepository;
import com.p4th.backend.util.HtmlImageUtils;
import com.p4th.backend.util.ULIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final AuthMapper authMapper;
    private final PostRepository postRepository;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public Page<PostListResponse> getPostsByBoard(String boardId, Pageable pageable) {
        try {
            Page<Post> posts = postRepository.findByBoardId(boardId, pageable);
            return posts.map(PostListResponse::from);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 목록 조회 중 오류: " + e.getMessage());
        }
    }

    @Transactional
    public Post getPostDetail(String postId, String userId) {
        try {
            // 조회수 1증가
            postMapper.incrementViewCount(postId);
            // 최근 본 게시물 테이블에 기록 삽입
            if (userId != null && !userId.trim().isEmpty()) {
                postMapper.insertPostView(userId, postId);
            }
            Post post = postMapper.getPostDetail(postId);
            if (post == null) {
                throw new CustomException(ErrorCode.POST_NOT_FOUND, "게시글을 찾을 수 없습니다.");
            }
            post.setComments(commentMapper.getCommentsByPost(postId));
            if (PostStatus.DELETED.equals(post.getStatus())) {
                post.setContent("삭제된 게시글입니다");
            }
            return post;
        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 상세 조회 중 오류: " + e.getMessage());
        }
    }

    /**
     * 게시글 작성 및 첨부파일 업로드를 한 번에 처리한다.
     * 작성 시, UserMapper를 이용해 사용자 정보를 조회하여 loginId를 설정하고,
     * created_by, updated_by 필드에 user_info.userId를 설정한다.
     *
     * @param boardId     게시판 ID
     * @param userId      작성자 ID (user_info.userId)
     * @param title       게시글 제목
     * @param content     게시글 내용
     * @return 생성된 게시글의 ID
     */
    @Transactional
    public String registerPost(String boardId, String userId, String title, String content) {
        try {
            User user = authMapper.selectByUserId(userId);
            if (user == null) {
                throw new CustomException(ErrorCode.USER_NOT_FOUND, "사용자 정보를 찾을 수 없습니다.");
            }
            // HTML 콘텐츠 내의 inline 미디어 처리
            String processedContent = HtmlImageUtils.processInlineMedia(content, boardId, s3Service);
            // 게시글 생성
            Post post = new Post();
            String postId = ULIDUtil.getULID();
            post.setPostId(postId);
            post.setBoardId(boardId);
            post.setUserId(userId);
            post.setTitle(title);
            post.setContent(processedContent);
            post.setStatus(PostStatus.NORMAL);
            int inserted = postMapper.insertPost(post);
            if (inserted != 1) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 등록 실패");
            }
            return postId;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 등록 중 오류: " + e.getMessage());
        }
    }


    @Transactional
    public void updatePost(String postId, String userId, String title, String content) {
        try {
            Post existing = postMapper.getPostDetail(postId);
            if (existing == null) {
                throw new CustomException(ErrorCode.POST_NOT_FOUND, "게시글을 찾을 수 없습니다.");
            }
            if (PostStatus.DELETED.equals(existing.getStatus())) {
                throw new CustomException(ErrorCode.POST_ALREADY_DELETED, "이미 삭제된 게시글입니다.");
            }
            if (!existing.getUserId().equals(userId)) {
                throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS, "본인이 작성한 게시글만 수정할 수 있습니다.");
            }
            String processedContent = HtmlImageUtils.processInlineMedia(content, existing.getBoardId(), s3Service);
            Post post = new Post();
            post.setPostId(postId);
            post.setUserId(userId);
            post.setTitle(title);
            post.setContent(processedContent);
            int updated = postMapper.updatePost(post);
            if (updated != 1) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 수정 실패");
            }
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 수정 중 오류: " + e.getMessage());
        }
    }

    @Transactional
    public void deletePost(String postId, String requesterUserId) {
        try {
            Post existing = postMapper.getPostDetail(postId);
            if (existing == null) {
                throw new CustomException(ErrorCode.POST_NOT_FOUND, "게시글을 찾을 수 없습니다.");
            }
            // 이미 삭제 상태인 경우 에러 발생
            if (PostStatus.DELETED.equals(existing.getStatus())) {
                throw new CustomException(ErrorCode.POST_ALREADY_DELETED, "이미 삭제된 게시글입니다.");
            }
            User requester = authMapper.selectByUserId(requesterUserId);
            if (!existing.getUserId().equals(requesterUserId) && (requester == null || requester.getAdminRole() != 1)) {
                throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS, "본인이 작성한 게시글만 삭제할 수 있습니다.");
            }
            if (existing.getCommentCount() == 0) {
                int deleted = postMapper.physicalDeletePost(postId);
                if (deleted != 1) {
                    throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 삭제 실패");
                }
            } else {
                int updated = postMapper.deletePost(postId); // 상태를 DELETED로 업데이트
                if (updated != 1) {
                    throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 삭제 실패");
                }
            }
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 삭제 중 오류: " + e.getMessage());
        }
    }

}
