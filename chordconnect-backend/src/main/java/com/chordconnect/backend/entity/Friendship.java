package com.chordconnect.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "friendships")
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;  // Changed from User object

    @Column(name = "friend_id", nullable = false)
    private Long friendId;  // Changed from User object

    @Column(name = "status")
    private String status; // "pending", "accepted", "blocked"

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Friendship() {
        this.createdAt = LocalDateTime.now();
        this.status = "pending";
    }

    public Friendship(Long userId, Long friendId) {  // Updated constructor
        this();
        this.userId = userId;
        this.friendId = friendId;
    }

    // Updated getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getFriendId() { return friendId; }
    public void setFriendId(Long friendId) { this.friendId = friendId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}