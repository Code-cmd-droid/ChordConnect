package com.chordconnect.backend.service;

import com.chordconnect.backend.model.ChatMessage;
import com.chordconnect.backend.model.User;
import com.chordconnect.backend.repository.ChatMessageRepository;
import com.chordconnect.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    public ChatMessage saveMessage(String roomId, Long userId, String content, ChatMessage.MessageType type) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            ChatMessage message = new ChatMessage();
            message.setRoomId(roomId);
            message.setUser(userOpt.get());
            message.setContent(content);
            message.setType(type);
            return chatMessageRepository.save(message);
        }
        return null;
    }

    public List<ChatMessage> getRoomHistory(String roomId) {
        return chatMessageRepository.findTop50ByRoomIdOrderByTimestampDesc(roomId);
    }

    public List<ChatMessage> getRecentMessages(String roomId, int limit) {
        return chatMessageRepository.findTop50ByRoomIdOrderByTimestampDesc(roomId)
                .stream()
                .limit(limit)
                .toList();
    }

    public void clearRoomHistory(String roomId) {
        chatMessageRepository.deleteByRoomId(roomId);
    }

    public ChatMessage createSystemMessage(String roomId, String content) {
        ChatMessage message = new ChatMessage();
        message.setRoomId(roomId);
        message.setContent(content);
        message.setType(ChatMessage.MessageType.SYSTEM);
        return chatMessageRepository.save(message);
    }
}