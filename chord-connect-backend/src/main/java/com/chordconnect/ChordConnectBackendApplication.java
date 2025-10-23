package com.chordconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChordConnectBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChordConnectBackendApplication.class, args);
        System.out.println("🚀 Chord Connect backend is running...");
    }
}
