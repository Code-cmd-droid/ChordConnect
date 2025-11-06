package com.chordconnect.backend.repository;

import com.chordconnect.backend.model.VideoRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRoomRepository extends JpaRepository<VideoRoom, String> {
    List<VideoRoom> findByIsActiveTrueOrderByLastActivityDesc();
    Optional<VideoRoom> findByRoomIdAndIsActiveTrue(String roomId);
    List<VideoRoom> findByCreatedById(Long userId);

    @Query("SELECT vr FROM VideoRoom vr WHERE vr.isActive = true AND vr.lastActivity < :cutoffTime")
    List<VideoRoom> findInactiveRooms(java.time.LocalDateTime cutoffTime);

    @Query("SELECT COUNT(vr) FROM VideoRoom vr WHERE vr.isActive = true")
    long countActiveRooms();
}