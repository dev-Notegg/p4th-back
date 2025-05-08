package com.p4th.backend.chat.controller;

import com.p4th.backend.chat.domain.ChatRoom;
import com.p4th.backend.chat.dto.request.ChatMessageRequest;
import com.p4th.backend.chat.dto.request.DmRoomRequest;
import com.p4th.backend.chat.dto.response.ChatMessageResponse;
import com.p4th.backend.chat.dto.response.ChatRoomListResponse;
import com.p4th.backend.chat.dto.response.ChatRoomResponse;
import com.p4th.backend.chat.service.ChatQueryService;
import com.p4th.backend.chat.service.ChatRoomCommandService;
import com.p4th.backend.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "채팅", description = "채팅 REST API")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;
    private final ChatQueryService chatQueryService;
    private final ChatRoomCommandService chatRoomCommandService;

    @Operation(summary = "메시지 목록 조회", description = "특정 채팅방의 메시지를 페이징 조회한다.")
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<Page<ChatMessageResponse>> getMessages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(chatQueryService.getMessages(roomId, page, size));
    }

    @Operation(summary = "메시지 전송 (Fallback REST)", description = "WebSocket 연결 불가 시 사용하는 REST 전송용 API")
    @PostMapping("/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @Valid @RequestBody ChatMessageRequest request
    ) {
        ChatMessageResponse response = chatService.sendMessage(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "DM 채팅방 생성 또는 조회", description = "상대방 ID를 기준으로 1:1 DM 채팅방을 반환합니다. 존재하지 않으면 새로 생성됩니다.")
    @PostMapping("/dm-room")
    public ResponseEntity<ChatRoomResponse> getOrCreateDmRoom(
            @RequestHeader("X-USER-ID") String myId,
            @RequestBody DmRoomRequest request
    ) {
        ChatRoom room = chatRoomCommandService.getOrCreateDmRoom(
                myId,
                request.getMyNickname(),
                request.getOpponentId(),
                request.getOpponentNickname()
        );

        return ResponseEntity.ok(ChatRoomResponse.of(room));
    }

    @Operation(summary = "참여 중인 DM 채팅방 목록 조회", description = "상대방 정보 및 접속 상태 포함")
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomListResponse>> getMyChatRooms(
            @RequestHeader("X-USER-ID") String userId
    ) {
        return ResponseEntity.ok(chatQueryService.getDmRooms(userId));
    }
}
