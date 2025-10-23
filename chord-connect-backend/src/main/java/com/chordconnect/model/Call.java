package com.chordconnect.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "calls")
public class Call {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long callerId;
    private Long receiverId;

    private String status; // "RINGING", "ONGOING", "ENDED"
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
