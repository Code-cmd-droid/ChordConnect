package com.chordconnect.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_preferences")
public class UserPreferences {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    private String instruments;
    private String genres;
    private String languages;
    private String gender;
    private String ageGroup;

    // Video call preferences
    private boolean autoJoinVideo = true;
    private boolean autoMuteMic = false;
    private boolean autoEnableVideo = true;
    private String preferredLayout = "GRID"; // GRID, SPEAKER, SIDEBAR
}