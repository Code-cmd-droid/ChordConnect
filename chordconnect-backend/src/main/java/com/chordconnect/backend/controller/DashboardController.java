package com.chordconnect.backend.controller;

import com.chordconnect.backend.dto.ApiResponse;
import com.chordconnect.backend.model.User;
import com.chordconnect.backend.model.VideoRoom;
import com.chordconnect.backend.service.UserService;
import com.chordconnect.backend.service.VideoRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:3000")
public class DashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private VideoRoomService videoRoomService;

    @GetMapping("/overview/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardOverview(@PathVariable Long userId) {
        try {
            // Get user's active rooms
            List<VideoRoom> userRooms = videoRoomService.getUserRooms(userId);

            // Get recent jammers
            List<User> recentJammers = userService.getRecentJammers(userId);

            // Get active rooms count
            long activeRoomsCount = videoRoomService.getActiveRoomCount();

            // Prepare dashboard data
            Map<String, Object> dashboardData = new HashMap<>();
            dashboardData.put("userRooms", userRooms);
            dashboardData.put("recentJammers", recentJammers);
            dashboardData.put("activeRoomsCount", activeRoomsCount);
            dashboardData.put("userStats", Map.of(
                    "totalRoomsCreated", userRooms.size(),
                    "recentConnections", recentJammers.size()
            ));

            return ResponseEntity.ok(ApiResponse.success(dashboardData, "Dashboard data retrieved"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get dashboard data: " + e.getMessage()));
        }
    }

    @GetMapping("/quick-stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getQuickStats() {
        try {
            long activeRooms = videoRoomService.getActiveRoomCount();

            Map<String, Object> stats = Map.of(
                    "activeRooms", activeRooms,
                    "totalUsers", 150, // Demo data
                    "onlineUsers", 42,  // Demo data
                    "callsToday", 18    // Demo data
            );

            return ResponseEntity.ok(ApiResponse.success(stats, "Quick stats retrieved"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get quick stats: " + e.getMessage()));
        }
    }
}