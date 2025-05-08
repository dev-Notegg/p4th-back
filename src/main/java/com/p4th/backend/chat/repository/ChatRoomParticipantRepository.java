package com.p4th.backend.chat.repository;

import com.p4th.backend.chat.domain.ChatRoomParticipant;
import com.p4th.backend.chat.domain.ChatRoomParticipantId;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, ChatRoomParticipantId> {

    List<ChatRoomParticipant> findByRoomId(String roomId);

    List<ChatRoomParticipant> findByUserId(String userId);

    Optional<ChatRoomParticipant> findByRoomIdAndUserId(String roomId, String userId);

    @Query("""
        SELECT p.userId FROM ChatRoomParticipant p
        WHERE p.roomId = :roomId AND p.userId <> :myId
    """)
    String findOpponentId(@Param("roomId") String roomId, @Param("myId") String myId);

    @Query("""
        SELECT p.nickname FROM ChatRoomParticipant p
        WHERE p.roomId = :roomId AND p.userId = :userId
    """)
    String findNickname(@Param("roomId") String roomId, @Param("userId") String userId);
}