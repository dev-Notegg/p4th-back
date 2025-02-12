package com.p4th.backend.service;

import com.p4th.backend.domain.Post;
import com.p4th.backend.domain.PostAttachment;
import com.p4th.backend.domain.User;
import com.p4th.backend.dto.response.PopularPostResponse;
import com.p4th.backend.dto.response.PostListDto;
import com.p4th.backend.mapper.CommentMapper;
import com.p4th.backend.mapper.PostAttachmentMapper;
import com.p4th.backend.mapper.PostHistoryLogMapper;
import com.p4th.backend.mapper.PostMapper;
import com.p4th.backend.mapper.UserMapper;
import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.repository.PostRepository;
import com.p4th.backend.util.ULIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final PostAttachmentMapper postAttachmentMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final S3Service s3Service;
    private final PostHistoryLogMapper postHistoryLogMapper;
    private final PostRepository postRepository;

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
    public String registerPost(String boardId, String userId, String title, String content, List<MultipartFile> attachments) {
        // 사용자 정보 조회
        User user = userMapper.selectByUserId(userId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND, "사용자 정보를 찾을 수 없습니다.");
        }

        // inline 이미지 처리: HTML 내 data:image(...) 태그를 S3 업로드 후 URL로 변경
        String processedContent = processInlineImages(content, boardId);

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
        // 첨부파일 업로드 처리
        processAttachments(attachments, postId, userId);

        return postId;
    }


    @Transactional
    public void updatePost(String postId, String boardId, String userId, String title, String content, List<MultipartFile> attachments) {
        // 권한 체크: 수정 요청자의 userId와 기존 게시글의 작성자 비교
        Post existing = postMapper.getPostDetail(postId);
        if (existing == null) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글을 찾을 수 없습니다.");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS, "본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        // inline 이미지 처리
        String processedContent = processInlineImages(content, boardId);

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
        // 첨부파일 처리
        processAttachments(attachments, postId, userId);
    }

    public void deletePost(String postId, String requesterUserId) {
        Post existing = postMapper.getPostDetail(postId);
        if (existing == null) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글을 찾을 수 없습니다.");
        }
        // 관리자 권한(예: admin_role == 1)이거나 본인 게시글일 때 삭제 가능
        User requester = userMapper.selectByUserId(requesterUserId);
        if (!existing.getUserId().equals(requesterUserId) && (requester == null || requester.getAdminRole() != 1)) {
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

    /**
     * HTML 컨텐츠 내의 inline base64 이미지 처리.
     *
     * @param content 원본 HTML 컨텐츠
     * @param boardId 게시글이 속한 게시판 ID (이미지 저장 경로에 사용)
     * @return inline 이미지가 업로드되어 URL로 교체된 HTML 컨텐츠
     */
    private String processInlineImages(String content, String boardId) {
        // Jsoup 라이브러리 사용 (lombok과 관계없음)
        org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(content);
        org.jsoup.select.Elements imgTags = doc.select("img[src^=data:image/]");
        for (org.jsoup.nodes.Element img : imgTags) {
            String dataUri = img.attr("src");
            int commaIndex = dataUri.indexOf(",");
            if (commaIndex > 0) {
                String metadata = dataUri.substring(0, commaIndex); // ex: "data:image/png;base64"
                String base64Data = dataUri.substring(commaIndex + 1);
                // 확장자 추출 (예: image/png -> png)
                String ext = metadata.substring(metadata.indexOf("/") + 1, metadata.indexOf(";")).toLowerCase();
                // base64 디코딩
                byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
                // 업로드할 파일명 생성 (예: ULID + 확장자)
                String attachmentId = ULIDUtil.getULID();
                String fileName = attachmentId + "." + ext;
                String keyDir = "posts/" + boardId;
                // S3Service에 byte[] 데이터를 업로드하는 메서드 호출
                String fileUrl = s3Service.upload(imageBytes, keyDir, fileName);
                // 이미지 태그의 src를 업로드된 URL로 교체
                img.attr("src", fileUrl);
            }
        }
        // body() 내부의 HTML만 리턴 (필요에 따라 전체 doc.html() 리턴 가능)
        return doc.body().html();
    }

    /**
     * 첨부파일 업로드 및 PostAttachment 레코드 저장을 처리한다.
     *
     * @param attachments 첨부파일 목록
     * @param postId      게시글 ID
     * @param userId      첨부파일 생성자 (게시글 작성자)
     */
    private void processAttachments(List<MultipartFile> attachments, String postId, String userId) {
        if (attachments != null && !attachments.isEmpty()) {
            for (MultipartFile file : attachments) {
                String attachmentId = ULIDUtil.getULID();
                String keyDir = "posts/" + postId; // posts/{postId} 폴더
                String originalFilename = file.getOriginalFilename();
                String ext = "";
                if (originalFilename != null && originalFilename.lastIndexOf('.') != -1) {
                    ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
                }
                // 이미지 확장자인 경우 "IMAGE", 아니면 "OTHER"
                String attachType = (ext.matches("^(?i)(jpg|jpeg|png|gif|bmp)$")) ? "IMAGE" : "OTHER";
                String fileName = attachmentId + "_" + originalFilename;
                String fileUrl = s3Service.upload(file, keyDir, fileName);
                PostAttachment attachment = new PostAttachment();
                attachment.setAttachmentId(attachmentId);
                attachment.setPostId(postId);
                attachment.setFileName(originalFilename);
                attachment.setFileUrl(fileUrl);
                attachment.setAttachType(attachType);
                attachment.setFileSize(file.getSize());
                attachment.setCreatedBy(userId);
                int insertedAttachment = postAttachmentMapper.insertAttachment(attachment);
                if (insertedAttachment != 1) {
                    throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "첨부파일 등록 실패");
                }
            }
        }
    }
}
