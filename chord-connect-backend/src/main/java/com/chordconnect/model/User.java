package com.chordconnect.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String displayName;
    private String email;
    private boolean online;

    // Explicit getters (in case Lombok isn't working)
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
