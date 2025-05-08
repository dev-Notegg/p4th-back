package com.p4th.backend.chat.repository;

import com.p4th.backend.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {

    @Query("""
        SELECT r FROM ChatRoom r
        JOIN ChatRoomParticipant p ON r.roomId = p.roomId
        WHERE p.userId = :userId AND r.roomType = 'DM'
    """)
    List<ChatRoom> findAllDmRoomsByUser(@Param("userId") String userId);
}
