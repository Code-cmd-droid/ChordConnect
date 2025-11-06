package com.chordconnect.backend.repository;

import com.chordconnect.backend.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRoomIdOrderByTimestampAsc(String roomId);
    List<ChatMessage> findTop50ByRoomIdOrderByTimestampDesc(String roomId);
    void deleteByRoomId(String roomId);
}