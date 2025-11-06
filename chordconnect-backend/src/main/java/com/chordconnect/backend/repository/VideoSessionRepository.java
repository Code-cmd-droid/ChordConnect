package com.chordconnect.backend.repository;

import com.chordconnect.backend.entity.VideoSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface VideoSessionRepository extends JpaRepository<VideoSession, Long> {

    Optional<VideoSession> findBySessionId(String sessionId);

    List<VideoSession> findByIsActiveTrueOrderByCreatedAtDesc();

    @Query("SELECT vs FROM VideoSession vs JOIN vs.participants p WHERE p.id = :userId AND vs.isActive = true")
    List<VideoSession> findActiveSessionsByParticipant(@Param("userId") Long userId);

    @Query("SELECT vs FROM VideoSession vs WHERE vs.host.id = :hostId AND vs.isActive = true")
    List<VideoSession> findActiveSessionsByHost(@Param("hostId") Long hostId);
}