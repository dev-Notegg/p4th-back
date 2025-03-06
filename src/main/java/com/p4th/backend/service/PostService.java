package com.p4th.backend.service;

import com.p4th.backend.domain.*;
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

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final AuthMapper authMapper;
    private final PostRepository postRepository;
    private final S3Service s3Service;
    private final ScrapMapper scrapMapper;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<PostListResponse> getPostsByBoard(String boardId, String userId, Pageable pageable) {
        try {
            // userId가 제공되면 차단 조건 적용, 없으면 기존 메서드 호출
            Page<Post> posts;
            if (userId == null || userId.trim().isEmpty()) {
                posts = postRepository.findByBoardId(boardId, pageable);
            } else {
                posts = postRepository.findByBoardIdExcludingBlocked(boardId, userId, pageable);
            }
            return posts.map(PostListResponse::from);
        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 목록 조회 중 오류: " + Arrays.toString(e.getStackTrace()));
        }
    }

    @Transactional
    public Post getPostDetail(String postId, String userId) {
        try {
            // 최근 본 게시글 기록 삽입
            if (userId != null && !userId.trim().isEmpty()) {
                postMapper.insertPostView(userId, postId);
            }
            Post post = postMapper.getPostDetail(postId, userId);
            if (post == null) {
                throw new CustomException(ErrorCode.POST_NOT_FOUND);
            }
            // 댓글 최근 10개만 설정
            List<Comment> comments = commentMapper.getCommentsByPost(postId, userId);
            if (comments.size() > 10) {
                comments = comments.subList(0, 10);
            }
            post.setComments(comments);

            // 스크랩 여부 체크: 로그인한 사용자인 경우만 처리
            if (userId != null && !userId.trim().isEmpty()) {
                Scrap scrap = scrapMapper.getScrapByPostAndUser(postId, userId);
                if (scrap != null) {
                    post.setScrapped(true);
                    post.setScrapId(scrap.getScrapId());
                } else {
                    post.setScrapped(false);
                    post.setScrapId(null);
                }
            }
            return post;
        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 상세 조회 중 오류: " + Arrays.toString(e.getStackTrace()));
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
                throw new CustomException(ErrorCode.USER_NOT_FOUND);
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

            // 만약 해당 게시판이 공지 게시판이면 공지 알림 생성
            Board board = postMapper.getBoardWithCategory(boardId);
            if (board != null && board.getCategory() != null && board.getCategory().isNotice()) {
                notificationService.notifyNoticePost(postId, userId, processedContent);
            }

            return postId;
        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 등록 중 오류: " + Arrays.toString(e.getStackTrace()));
        }
    }


    @Transactional
    public void updatePost(String postId, String userId, String title, String content) {
        try {
            Post existing = postMapper.getPostDetail(postId, userId);
            if (existing == null) {
                throw new CustomException(ErrorCode.POST_NOT_FOUND);
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
        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 수정 중 오류: " + Arrays.toString(e.getStackTrace()));
        }
    }

    @Transactional
    public void deletePost(String postId, String userId) {
        try {
            Post existing = postMapper.getPostDetail(postId, userId);
            if (existing == null) {
                throw new CustomException(ErrorCode.POST_NOT_FOUND);
            }
            User requester = authMapper.selectByUserId(userId);
            if (!existing.getUserId().equals(userId) && (requester == null || requester.getAdminRole() != 1)) {
                throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS, "본인이 작성한 게시글만 삭제할 수 있습니다.");
            }
            int deleted = postMapper.physicalDeletePost(postId);
            if (deleted != 1) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 삭제 실패");
            }
        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 삭제 중 오류: " + e.getMessage());
        }
    }

    @Transactional
    public void incrementPostViewCount(String postId) {
        try {
            // 조회수 증가
            int updated = postMapper.incrementViewCount(postId);
            if (updated != 1) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "조회수 증가 실패");
            }
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "조회수 증가 중 오류: " + e.getMessage());
        }
    }
}
