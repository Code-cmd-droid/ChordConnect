package com.chordconnect.backend.service;

import com.chordconnect.backend.model.User;
import com.chordconnect.backend.model.VideoRoom;
import com.chordconnect.backend.repository.VideoRoomRepository;
import com.chordconnect.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class VideoRoomService {

    @Autowired
    private VideoRoomRepository videoRoomRepository;

    @Autowired
    private UserRepository userRepository;

    public VideoRoom createRoom(String roomName, User createdBy, Integer maxParticipants,
                                boolean waitingRoomEnabled, boolean recordingEnabled) {
        VideoRoom room = new VideoRoom();
        room.setRoomId(generateRoomId());
        room.setRoomName(roomName);
        room.setCreatedBy(createdBy);
        room.setMaxParticipants(maxParticipants != null ? maxParticipants : 12);
        room.setWaitingRoomEnabled(waitingRoomEnabled);
        room.setRecordingEnabled(recordingEnabled);

        // Add creator as first participant
        room.getParticipants().add(createdBy);

        return videoRoomRepository.save(room);
    }

    public Optional<VideoRoom> getRoom(String roomId) {
        return videoRoomRepository.findByRoomIdAndIsActiveTrue(roomId);
    }

    public List<VideoRoom> getActiveRooms() {
        return videoRoomRepository.findByIsActiveTrueOrderByLastActivityDesc();
    }

    public List<VideoRoom> getUserRooms(Long userId) {
        return videoRoomRepository.findByCreatedById(userId);
    }

    public boolean joinRoom(String roomId, Long userId) {
        Optional<VideoRoom> roomOpt = videoRoomRepository.findByRoomIdAndIsActiveTrue(roomId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (roomOpt.isPresent() && userOpt.isPresent()) {
            VideoRoom room = roomOpt.get();
            User user = userOpt.get();

            if (room.isFull()) {
                return false;
            }

            room.getParticipants().add(user);
            room.updateActivity();
            videoRoomRepository.save(room);
            return true;
        }
        return false;
    }

    public boolean leaveRoom(String roomId, Long userId) {
        Optional<VideoRoom> roomOpt = videoRoomRepository.findByRoomIdAndIsActiveTrue(roomId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (roomOpt.isPresent() && userOpt.isPresent()) {
            VideoRoom room = roomOpt.get();
            User user = userOpt.get();

            room.getParticipants().remove(user);
            room.updateActivity();

            // If room is empty, deactivate it
            if (room.getParticipants().isEmpty()) {
                room.setActive(false);
            }

            videoRoomRepository.save(room);
            return true;
        }
        return false;
    }

    public void closeRoom(String roomId) {
        Optional<VideoRoom> roomOpt = videoRoomRepository.findByRoomIdAndIsActiveTrue(roomId);
        if (roomOpt.isPresent()) {
            VideoRoom room = roomOpt.get();
            room.setActive(false);
            videoRoomRepository.save(room);
        }
    }

    public void cleanupInactiveRooms() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(2);
        List<VideoRoom> inactiveRooms = videoRoomRepository.findInactiveRooms(cutoffTime);

        for (VideoRoom room : inactiveRooms) {
            room.setActive(false);
        }

        videoRoomRepository.saveAll(inactiveRooms);
    }

    private String generateRoomId() {
        return "room-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public long getActiveRoomCount() {
        return videoRoomRepository.countActiveRooms();
    }
}