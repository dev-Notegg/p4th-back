package com.p4th.backend.service;

import com.p4th.backend.controller.PostController;
import com.p4th.backend.domain.Post;
import com.p4th.backend.domain.PostAttachment;
import com.p4th.backend.domain.User;
import com.p4th.backend.mapper.CommentMapper;
import com.p4th.backend.mapper.PostAttachmentMapper;
import com.p4th.backend.mapper.PostMapper;
import com.p4th.backend.mapper.UserMapper;
import com.p4th.backend.dto.PageResponse;
import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.amazonaws.services.s3.AmazonS3;
import com.p4th.backend.config.S3Config;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final PostAttachmentMapper postAttachmentMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final AmazonS3 amazonS3;
    private final S3Config s3Config;

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
        if(user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND, "사용자 정보를 찾을 수 없습니다.");
        }
        // 게시글 생성
        Post post = new Post();
        String postId = UUID.randomUUID().toString();
        post.setPostId(postId);
        post.setBoardId(boardId);
        post.setUserId(userId);
        post.setLoginId(user.getLoginId());
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
                String key = "posts/" + postId + "/" + attachmentId + "_" + file.getOriginalFilename();
                try {
                    amazonS3.putObject(s3Config.getBucketName(), key, file.getInputStream(), null);
                } catch (IOException e) {
                    throw new CustomException(ErrorCode.S3_UPLOAD_FAILED, e.getMessage());
                }
                String fileUrl = s3Config.getEndPoint() + "/" + s3Config.getBucketName() + "/" + key;
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
     * 게시글 수정 시 본문 업데이트와 첨부파일 추가 및 삭제를 처리한다.
     *
     * @param postId              수정할 게시글 ID
     * @param request             수정 요청 DTO (boardId, userId, title, content 포함)
     * @param newAttachments      새로 추가할 첨부파일 목록 (없을 수 있음)
     * @param removeAttachmentIds 삭제할 첨부파일 ID 목록 (없을 수 있음)
     */
    @Transactional
    public void updatePostWithAttachments(String postId, PostController.UpdatePostRequest request,
                                          List<MultipartFile> newAttachments, List<String> removeAttachmentIds) {
        // 게시글 내용 수정
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
        // 삭제할 첨부파일 처리
        if (removeAttachmentIds != null && !removeAttachmentIds.isEmpty()) {
            for (String attachmentId : removeAttachmentIds) {
                PostAttachment attachment = postAttachmentMapper.getAttachmentById(attachmentId);
                if (attachment != null) {
                    String fileUrl = attachment.getFileUrl();
                    String prefix = s3Config.getEndPoint() + "/" + s3Config.getBucketName() + "/";
                    String key = fileUrl.substring(prefix.length());
                    amazonS3.deleteObject(s3Config.getBucketName(), key);
                    int deleted = postAttachmentMapper.deleteAttachment(attachmentId);
                    if (deleted != 1) {
                        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "첨부파일 삭제 실패");
                    }
                }
            }
        }
        // 신규 첨부파일 처리
        if (newAttachments != null && !newAttachments.isEmpty()) {
            for (MultipartFile file : newAttachments) {
                String attachmentId = UUID.randomUUID().toString();
                String folder = "posts/";
                if (file.getContentType() != null && file.getContentType().startsWith("video/")) {
                    folder += "videos/";
                } else {
                    folder += "images/";
                }
                String key = folder + postId + "/" + attachmentId + "_" + file.getOriginalFilename();
                try {
                    amazonS3.putObject(s3Config.getBucketName(), key, file.getInputStream(), null);
                } catch (IOException e) {
                    throw new CustomException(ErrorCode.S3_UPLOAD_FAILED, e.getMessage());
                }
                String fileUrl = s3Config.getEndPoint() + "/" + s3Config.getBucketName() + "/" + key;
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

    public void deletePost(String postId) {
        int deleted = postMapper.deletePost(postId);
        if (deleted != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 삭제 실패");
        }
    }
}
