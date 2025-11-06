package com.chordconnect.backend.controller;

import com.chordconnect.backend.dto.ApiResponse;
import com.chordconnect.backend.model.ChatMessage;
import com.chordconnect.backend.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:3000")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/rooms/{roomId}/history")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getChatHistory(@PathVariable String roomId) {
        try {
            List<ChatMessage> history = chatService.getRoomHistory(roomId);
            return ResponseEntity.ok(ApiResponse.success(history, "Chat history retrieved"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get chat history: " + e.getMessage()));
        }
    }

    @GetMapping("/rooms/{roomId}/recent")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getRecentMessages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<ChatMessage> messages = chatService.getRecentMessages(roomId, limit);
            return ResponseEntity.ok(ApiResponse.success(messages, "Recent messages retrieved"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get recent messages: " + e.getMessage()));
        }
    }

    @DeleteMapping("/rooms/{roomId}/clear")
    public ResponseEntity<ApiResponse<String>> clearChatHistory(@PathVariable String roomId) {
        try {
            chatService.clearRoomHistory(roomId);
            return ResponseEntity.ok(ApiResponse.success("Chat history cleared"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to clear chat history: " + e.getMessage()));
        }
    }
}