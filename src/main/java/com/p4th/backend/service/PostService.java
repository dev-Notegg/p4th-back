package com.p4th.backend.service;

import com.p4th.backend.controller.PostController;
import com.p4th.backend.domain.Post;
import com.p4th.backend.domain.PostAttachment;
import com.p4th.backend.domain.User;
import com.p4th.backend.dto.PopularPostResponse;
import com.p4th.backend.mapper.CommentMapper;
import com.p4th.backend.mapper.PostAttachmentMapper;
import com.p4th.backend.mapper.PostHistoryLogMapper;
import com.p4th.backend.mapper.PostMapper;
import com.p4th.backend.mapper.UserMapper;
import com.p4th.backend.dto.PageResponse;
import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final PostAttachmentMapper postAttachmentMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final S3Service s3Service;
    private final PostHistoryLogMapper postHistoryLogMapper;

    public PageResponse<Post> getPostsByBoard(String boardId, int page, int size) {
        int offset = page * size;
        List<Post> posts = postMapper.getPostsByBoard(boardId, size, offset);
        int totalElements = postMapper.countPostsByBoard(boardId);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        PageResponse<Post> response = new PageResponse<>();
        response.setContent(posts);
        PageResponse.Pageable pageable = new PageResponse.Pageable();
        pageable.setPageNumber(page);
        pageable.setPageSize(size);
        response.setPageable(pageable);
        response.setTotalElements(totalElements);
        response.setTotalPages(totalPages);
        return response;
    }

    public Post getPostDetail(String postId) {
        Post post = postMapper.getPostDetail(postId);
        if (post != null) {
            post.setAttachments(postAttachmentMapper.getAttachmentsByPost(postId));
            post.setComments(commentMapper.getCommentsByPost(postId));
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
     * @param attachments 첨부파일 목록 (없을 수 있음)
     * @return 생성된 게시글의 ID
     */
    @Transactional
    public String registerPostWithAttachments(String boardId, String userId, String title, String content, List<MultipartFile> attachments) {
        // 사용자 정보 조회
        User user = userMapper.selectByUserId(userId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND, "사용자 정보를 찾을 수 없습니다.");
        }
        // 게시글 생성
        Post post = new Post();
        String postId = UUID.randomUUID().toString();
        post.setPostId(postId);
        post.setBoardId(boardId);
        post.setUserId(userId);
        post.setTitle(title);
        post.setContent(content);
        int inserted = postMapper.insertPost(post);
        if (inserted != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 등록 실패");
        }
        // 첨부파일 업로드 처리
        if (attachments != null && !attachments.isEmpty()) {
            for (MultipartFile file : attachments) {
                String attachmentId = UUID.randomUUID().toString();
                String keyDir = "posts/" + postId; // posts 폴더 내에 postId 폴더 생성
                String fileName = attachmentId + "_" + file.getOriginalFilename();
                String fileUrl = s3Service.upload(file, keyDir, fileName);
                PostAttachment attachment = new PostAttachment();
                attachment.setAttachmentId(attachmentId);
                attachment.setPostId(postId);
                attachment.setFileName(file.getOriginalFilename());
                attachment.setFileUrl(fileUrl);
                attachment.setAttachType("IMAGE");
                attachment.setFileSize(file.getSize());
                attachment.setCreatedBy(userId);
                int insertedAttachment = postAttachmentMapper.insertAttachment(attachment);
                if (insertedAttachment != 1) {
                    throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "첨부파일 등록 실패");
                }
            }
        }
        return postId;
    }

    /**
     * 게시글 수정 시, 게시글 본문 업데이트와 함께 기존 첨부파일을 모두 삭제한 후,
     * 클라이언트가 전송한 첨부파일 목록으로 교체한다.
     *
     * @param postId         수정할 게시글 ID
     * @param request        수정 요청 DTO (boardId, userId, title, content 포함)
     * @param newAttachments 새로 전송된 첨부파일 목록 (없을 수 있음)
     */
    @Transactional
    public void updatePostWithAttachments(String postId, PostController.UpdatePostRequest request,
                                          List<MultipartFile> newAttachments) {
        // 권한 체크: 수정 요청자의 userId와 기존 게시글의 작성자 비교
        Post existing = postMapper.getPostDetail(postId);
        if (existing == null) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글을 찾을 수 없습니다.");
        }
        if (!existing.getUserId().equals(request.getUserId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS, "본인이 작성한 게시글만 수정할 수 있습니다.");
        }
        Post post = new Post();
        post.setPostId(postId);
        post.setBoardId(request.getBoardId());
        post.setUserId(request.getUserId());
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        int updated = postMapper.updatePost(post);
        if (updated != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 수정 실패");
        }
        // 기존 첨부파일 전체 삭제 처리
        List<PostAttachment> existingAttachments = postAttachmentMapper.getAttachmentsByPost(postId);
        if (existingAttachments != null && !existingAttachments.isEmpty()) {
            for (PostAttachment attachment : existingAttachments) {
                s3Service.deleteByFileUrl(attachment.getFileUrl());
                int deleted = postAttachmentMapper.deleteAttachment(attachment.getAttachmentId());
                if (deleted != 1) {
                    throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "기존 첨부파일 삭제 실패");
                }
            }
        }
        // 신규 첨부파일 추가 처리
        if (newAttachments != null && !newAttachments.isEmpty()) {
            for (MultipartFile file : newAttachments) {
                String attachmentId = UUID.randomUUID().toString();
                String keyDir = "posts/" + postId;
                String fileName = attachmentId + "_" + file.getOriginalFilename();
                String fileUrl = s3Service.upload(file, keyDir, fileName);
                PostAttachment attachment = new PostAttachment();
                attachment.setAttachmentId(attachmentId);
                attachment.setPostId(postId);
                attachment.setFileName(file.getOriginalFilename());
                attachment.setFileUrl(fileUrl);
                attachment.setAttachType("IMAGE");
                attachment.setFileSize(file.getSize());
                attachment.setCreatedBy(request.getUserId());
                int insertedAttachment = postAttachmentMapper.insertAttachment(attachment);
                if (insertedAttachment != 1) {
                    throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "새 첨부파일 등록 실패");
                }
            }
        }
    }

    public void deletePost(String postId, String requesterUserId) {
        Post existing = postMapper.getPostDetail(postId);
        if (existing == null) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글을 찾을 수 없습니다.");
        }
        if (!existing.getUserId().equals(requesterUserId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS, "본인이 작성한 게시글만 삭제할 수 있습니다.");
        }
        List<PostAttachment> attachments = postAttachmentMapper.getAttachmentsByPost(postId);
        if (attachments != null && !attachments.isEmpty()) {
            for (PostAttachment attachment : attachments) {
                s3Service.deleteByFileUrl(attachment.getFileUrl());
                int deleted = postAttachmentMapper.deleteAttachment(attachment.getAttachmentId());
                if (deleted != 1) {
                    throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "첨부파일 삭제 실패");
                }
            }
        }
        int deleted = postMapper.deletePost(postId);
        if (deleted != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 삭제 실패");
        }
    }

    public List<PopularPostResponse> getPopularPosts(String period) {
        return postHistoryLogMapper.getPopularPostsByPeriod(period);
    }
}
