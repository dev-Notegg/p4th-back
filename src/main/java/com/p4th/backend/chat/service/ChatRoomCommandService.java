package com.p4th.backend.chat.service;

import com.p4th.backend.chat.domain.ChatRoom;
import com.p4th.backend.chat.domain.ChatRoomParticipant;
import com.p4th.backend.chat.repository.ChatRoomParticipantRepository;
import com.p4th.backend.chat.repository.ChatRoomRepository;
import com.p4th.backend.util.ULIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomCommandService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;

    /**
     * A, B 사용자 간 DM 채팅방이 존재하면 반환, 없으면 생성
     */
    @Transactional
    public ChatRoom getOrCreateDmRoom(String userA, String userAName, String userB, String userBName) {
        List<ChatRoomParticipant> userARooms = chatRoomParticipantRepository.findByUserId(userA);

        for (ChatRoomParticipant p : userARooms) {
            String roomId = p.getRoomId();
            ChatRoom room = chatRoomRepository.findById(roomId).orElse(null);
            if (room != null && room.getRoomType() == ChatRoom.RoomType.DM) {
                List<ChatRoomParticipant> participants = chatRoomParticipantRepository.findByRoomId(roomId);
                if (participants.size() == 2 &&
                        participants.stream().anyMatch(x -> x.getUserId().equals(userB))) {
                    return room;
                }
            }
        }

        // 생성
        String newRoomId = ULIDUtil.getULID();
        ChatRoom newRoom = ChatRoom.builder()
                .roomId(newRoomId)
                .roomType(ChatRoom.RoomType.DM)
                .createdBy(userA)
                .build();
        chatRoomRepository.save(newRoom);

        ChatRoomParticipant a = ChatRoomParticipant.builder()
                .roomId(newRoomId)
                .userId(userA)
                .nickname(userAName)
                .build();
        ChatRoomParticipant b = ChatRoomParticipant.builder()
                .roomId(newRoomId)
                .userId(userB)
                .nickname(userBName)
                .build();

        chatRoomParticipantRepository.saveAll(List.of(a, b));

        return newRoom;
    }
}
