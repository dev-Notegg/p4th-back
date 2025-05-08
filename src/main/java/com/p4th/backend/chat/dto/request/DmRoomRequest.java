package com.p4th.backend.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DmRoomRequest {

    @Schema(description = "요청자 닉네임", example = "체리버터")
    private String myNickname;

    @Schema(description = "상대방 사용자 ID", example = "userB")
    private String opponentId;

    @Schema(description = "상대방 닉네임", example = "민트초코")
    private String opponentNickname;
}
