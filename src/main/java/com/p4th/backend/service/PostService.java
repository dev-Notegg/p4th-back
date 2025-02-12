package com.p4th.backend.service;

import com.p4th.backend.domain.Post;
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
import com.p4th.backend.util.ULIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
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
     * @return 생성된 게시글의 ID
     */
    @Transactional
    public String registerPost(String boardId, String userId, String title, String content) {
        // 사용자 정보 조회
        User user = userMapper.selectByUserId(userId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND, "사용자 정보를 찾을 수 없습니다.");
        }

        // HTML 컨텐츠 내의 inline 미디어 처리 (예: base64 이미지 업로드 후 URL 교체)
        String processedContent = processInlineMedia(content, boardId);

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

        // HTML 내 inline 미디어 처리
        String processedContent = processInlineMedia(content, boardId);

        // 게시글 업데이트
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
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글을 찾을 수 없습니다.");
        }
        // 관리자 권한(예: admin_role == 1)이거나 본인 게시글일 때 삭제 가능
        User requester = userMapper.selectByUserId(requesterUserId);
        if (!existing.getUserId().equals(requesterUserId) && (requester == null || requester.getAdminRole() != 1)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS, "본인이 작성한 게시글만 삭제할 수 있습니다.");
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
     * HTML 컨텐츠 내의 inline 미디어 처리
     *
     * @param content 원본 HTML 컨텐츠
     * @param boardId 게시글이 속한 게시판 ID (이미지 저장 경로에 사용)
     * @return inline 미디어가 처리되어 URL로 교체된 HTML 컨텐츠
     */
    private String processInlineMedia(String content, String boardId) {
        org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(content);
        org.jsoup.select.Elements imgTags = doc.select("img[src^=data:image/]");
        for (org.jsoup.nodes.Element img : imgTags) {
            String dataUri = img.attr("src");
            int commaIndex = dataUri.indexOf(",");
            if (commaIndex > 0) {
                String metadata = dataUri.substring(0, commaIndex);  // 예: "data:image/png;base64"
                String base64Data = dataUri.substring(commaIndex + 1);
                // 확장자 추출 (예: image/png -> png)
                String ext = metadata.substring(metadata.indexOf("/") + 1, metadata.indexOf(";")).toLowerCase();
                byte[] mediaBytes = java.util.Base64.getDecoder().decode(base64Data);
                // 파일명 생성 (예: ULID + 확장자)
                String attachmentId = ULIDUtil.getULID();
                String fileName = attachmentId + "." + ext;
                String keyDir = "posts/" + boardId;
                // S3Service의 byte[] 업로드 메서드 호출
                String fileUrl = s3Service.upload(mediaBytes, keyDir, fileName);
                // 이미지 태그의 src 값을 업로드된 URL로 교체
                img.attr("src", fileUrl);
                // (필요 시, attachType을 로그로 남기거나 DB에 저장하는 로직 추가 가능)
            }
        }
        // 문서의 body 내부 HTML 반환 (doc.body().html())
        return doc.body().html();
    }
}
