package com.p4th.backend.service;

import com.p4th.backend.domain.Post;
import com.p4th.backend.domain.PostStatus;
import com.p4th.backend.domain.User;
import com.p4th.backend.dto.response.PopularPostResponse;
import com.p4th.backend.dto.response.PostListDto;
import com.p4th.backend.mapper.CommentMapper;
import com.p4th.backend.mapper.PostHistoryLogMapper;
import com.p4th.backend.mapper.PostMapper;
import com.p4th.backend.mapper.UserMapper;
import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.repository.PostRepository;
import com.p4th.backend.util.HtmlImageUtils;
import com.p4th.backend.util.RelativeTimeFormatter;
import com.p4th.backend.util.ULIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final PostHistoryLogMapper postHistoryLogMapper;
    private final PostRepository postRepository;
    private static final DateTimeFormatter originalFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public Page<PostListDto> getPostsByBoard(String boardId, Pageable pageable) {
        Page<Post> posts = postRepository.findByBoardId(boardId, pageable);
        return posts.map(PostListDto::from);
    }

    @Transactional
    public Post getPostDetail(String postId) {
        // 조회수 1증가
        postMapper.incrementViewCount(postId);
        Post post = postMapper.getPostDetail(postId);
        if (post == null) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }
        post.setComments(commentMapper.getCommentsByPost(postId));
        if (post.getStatus() == PostStatus.DELETED) {
            post.setContent("삭제된 게시글입니다");
        }
        return post;
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
        // 사용자 정보 조회
        User user = userMapper.selectByUserId(userId);
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
        int inserted = postMapper.insertPost(post);
        if (inserted != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 등록 실패");
        }
        return postId;
    }


    @Transactional
    public void updatePost(String postId, String boardId, String userId, String title, String content) {
        // 권한 체크: 수정 요청자의 userId와 기존 게시글의 작성자 비교
        Post existing = postMapper.getPostDetail(postId);
        if (existing == null) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글을 찾을 수 없습니다.");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS, "본인이 작성한 게시글만 수정할 수 있습니다.");
        }
        String processedContent = HtmlImageUtils.processInlineMedia(content, boardId, s3Service);
        Post post = new Post();
        post.setPostId(postId);
        post.setBoardId(boardId);
        post.setUserId(userId);
        post.setTitle(title);
        post.setContent(processedContent);
        int updated = postMapper.updatePost(post);
        if (updated != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 수정 실패");
        }
    }

    public void deletePost(String postId, String requesterUserId) {
        Post existing = postMapper.getPostDetail(postId);
        if (existing == null) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }
        // 관리자 권한(예: admin_role == 1)이거나 본인 게시글일 때 삭제 가능
        User requester = userMapper.selectByUserId(requesterUserId);
        if (!existing.getUserId().equals(requesterUserId) && (requester == null || requester.getAdminRole() != 1)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS, "본인이 작성한 게시글만 삭제할 수 있습니다.");
        }
        // 게시글에 댓글이 없는 경우 -> 물리 삭제
        if (existing.getCommentCount() == 0) {
            int deleted = postMapper.physicalDeletePost(postId);
            if (deleted != 1) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 삭제 실패");
            }
        } else {
            // 댓글이 있는 경우 상태 업데이트 처리
            int updated = postMapper.deletePost(postId); // 상태를 DELETED로 업데이트
            if (updated != 1) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 삭제 실패");
            }
        }
    }

    public List<PopularPostResponse> getPopularPosts(String period) {
        List<PopularPostResponse> responses = postHistoryLogMapper.getPopularPostsByPeriod(period);
        responses.forEach(response -> {
            // imageUrl, imageCount는 content 필드에서 추출
            if (response.getContent() != null && !response.getContent().isEmpty()) {
                String imgUrl = HtmlImageUtils.extractFirstImageUrl(response.getContent());
                int imgCount = HtmlImageUtils.countInlineImages(response.getContent());
                response.setImageUrl(imgUrl);
                response.setImageCount(imgCount);
            }

            if (response.getCreatedAt() != null && !response.getCreatedAt().isEmpty()) {
                LocalDateTime createdTime = LocalDateTime.parse(response.getCreatedAt(), originalFormatter);
                response.setCreatedAt(RelativeTimeFormatter.formatRelativeTime(createdTime));
            }
        });
        return responses;
    }

}
