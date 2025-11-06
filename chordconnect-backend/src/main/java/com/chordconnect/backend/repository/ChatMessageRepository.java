package com.chordconnect.backend.repository;

import com.chordconnect.backend.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.roomId = :roomId ORDER BY cm.timestamp ASC")
    List<ChatMessage> findByRoomIdOrderByTimestampAsc(@Param("roomId") Long roomId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.roomId = :roomId ORDER BY cm.timestamp DESC LIMIT :limit")
    List<ChatMessage> findRecentByRoomId(@Param("roomId") Long roomId, @Param("limit") int limit);
}