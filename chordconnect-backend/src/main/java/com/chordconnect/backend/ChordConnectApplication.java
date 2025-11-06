package com.chordconnect.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration;

@SpringBootApplication(exclude = {
        SecurityAutoConfiguration.class,
        WebSocketServletAutoConfiguration.class  // Disable WebSocket temporarily
})
public class ChordConnectApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChordConnectApplication.class, args);
    }
}