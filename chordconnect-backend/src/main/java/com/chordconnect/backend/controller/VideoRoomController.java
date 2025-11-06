package com.chordconnect.backend.controller;

import com.chordconnect.backend.dto.ApiResponse;
import com.chordconnect.backend.dto.CreateRoomRequest;
import com.chordconnect.backend.model.User;
import com.chordconnect.backend.model.VideoRoom;
import com.chordconnect.backend.service.UserService;
import com.chordconnect.backend.service.VideoRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/video")
@CrossOrigin(origins = "http://localhost:3000")
public class VideoRoomController {

    @Autowired
    private VideoRoomService videoRoomService;

    @Autowired
    private UserService userService;

    @PostMapping("/rooms")
    public ResponseEntity<ApiResponse<VideoRoom>> createRoom(
            @RequestBody CreateRoomRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // Extract user ID from token (simplified - in real app, use proper authentication)
            Long userId = extractUserIdFromToken(token);
            Optional<User> userOpt = userService.findById(userId);

            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("User not found"));
            }

            VideoRoom room = videoRoomService.createRoom(
                    request.getRoomName(),
                    userOpt.get(),
                    request.getMaxParticipants(),
                    request.isWaitingRoomEnabled(),
                    request.isRecordingEnabled()
            );

            return ResponseEntity.ok(ApiResponse.success(room, "Room created successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create room: " + e.getMessage()));
        }
    }

    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<VideoRoom>>> getActiveRooms() {
        try {
            List<VideoRoom> rooms = videoRoomService.getActiveRooms();
            return ResponseEntity.ok(ApiResponse.success(rooms, "Active rooms retrieved"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get rooms: " + e.getMessage()));
        }
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<VideoRoom>> getRoom(@PathVariable String roomId) {
        try {
            Optional<VideoRoom> roomOpt = videoRoomService.getRoom(roomId);
            if (roomOpt.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(roomOpt.get(), "Room found"));
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get room: " + e.getMessage()));
        }
    }

    @PostMapping("/rooms/{roomId}/join")
    public ResponseEntity<ApiResponse<Map<String, Object>>> joinRoom(
            @PathVariable String roomId,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = extractUserIdFromToken(token);
            boolean success = videoRoomService.joinRoom(roomId, userId);

            if (success) {
                Map<String, Object> response = Map.of(
                        "roomId", roomId,
                        "userId", userId,
                        "message", "Joined room successfully"
                );
                return ResponseEntity.ok(ApiResponse.success(response, "Joined room"));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Failed to join room - room may be full or not found"));
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to join room: " + e.getMessage()));
        }
    }

    @PostMapping("/rooms/{roomId}/leave")
    public ResponseEntity<ApiResponse<String>> leaveRoom(
            @PathVariable String roomId,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = extractUserIdFromToken(token);
            boolean success = videoRoomService.leaveRoom(roomId, userId);

            if (success) {
                return ResponseEntity.ok(ApiResponse.success("Left room successfully"));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Failed to leave room"));
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to leave room: " + e.getMessage()));
        }
    }

    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<String>> closeRoom(@PathVariable String roomId) {
        try {
            videoRoomService.closeRoom(roomId);
            return ResponseEntity.ok(ApiResponse.success("Room closed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to close room: " + e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        try {
            long activeRooms = videoRoomService.getActiveRoomCount();
            Map<String, Object> stats = Map.of(
                    "activeRooms", activeRooms,
                    "timestamp", java.time.LocalDateTime.now()
            );
            return ResponseEntity.ok(ApiResponse.success(stats, "Stats retrieved"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get stats: " + e.getMessage()));
        }
    }

    // Helper method to extract user ID from token (simplified)
    private Long extractUserIdFromToken(String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            // In real implementation, use JwtService to extract user ID
            // For demo, return a default user ID
            return 1L;
        } catch (Exception e) {
            throw new RuntimeException("Invalid token");
        }
    }
}