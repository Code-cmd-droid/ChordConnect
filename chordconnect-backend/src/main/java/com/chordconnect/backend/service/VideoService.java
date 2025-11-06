package com.chordconnect.backend.service;

import com.chordconnect.backend.entity.VideoSession;
import com.chordconnect.backend.entity.User;
import com.chordconnect.backend.repository.VideoSessionRepository;
import com.chordconnect.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class VideoService {

    @Autowired
    private VideoSessionRepository videoSessionRepository;

    @Autowired
    private UserRepository userRepository;

    public VideoSession createSession(Long hostId, String title) {
        Optional<User> hostOpt = userRepository.findById(hostId);
        if (hostOpt.isEmpty()) {
            throw new RuntimeException("Host user not found");
        }

        String sessionId = UUID.randomUUID().toString().substring(0, 8);
        VideoSession session = new VideoSession(sessionId, hostOpt.get(), title);
        session.addParticipant(hostOpt.get());

        return videoSessionRepository.save(session);
    }

    public VideoSession joinSession(String sessionId, Long userId) {
        Optional<VideoSession> sessionOpt = videoSessionRepository.findBySessionId(sessionId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (sessionOpt.isEmpty() || userOpt.isEmpty()) {
            throw new RuntimeException("Session or user not found");
        }

        VideoSession session = sessionOpt.get();
        if (!session.getIsActive()) {
            throw new RuntimeException("Session is not active");
        }

        session.addParticipant(userOpt.get());
        return videoSessionRepository.save(session);
    }

    public void leaveSession(String sessionId, Long userId) {
        Optional<VideoSession> sessionOpt = videoSessionRepository.findBySessionId(sessionId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (sessionOpt.isPresent() && userOpt.isPresent()) {
            VideoSession session = sessionOpt.get();
            session.removeParticipant(userOpt.get());
            videoSessionRepository.save(session);
        }
    }

    public List<VideoSession> getActiveSessions() {
        return videoSessionRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }

    public Optional<VideoSession> getSession(String sessionId) {
        return videoSessionRepository.findBySessionId(sessionId);
    }
}