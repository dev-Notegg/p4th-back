package com.p4th.backend.mapper;

import com.p4th.backend.domain.PostAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PostAttachmentMapper {
    List<PostAttachment> getAttachmentsByPost(@Param("postId") String postId);

    int insertAttachment(PostAttachment attachment);

    int deleteAttachment(@Param("attachmentId") String attachmentId);

    PostAttachment getAttachmentById(@Param("attachmentId") String attachmentId);
}
