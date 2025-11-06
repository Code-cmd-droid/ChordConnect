package com.chordconnect.backend.dto;

public class CreateRoomRequest {
    private String roomName;
    private Integer maxParticipants = 12;
    private boolean waitingRoomEnabled = false;
    private boolean recordingEnabled = false;

    // Getters and Setters
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }
    public boolean isWaitingRoomEnabled() { return waitingRoomEnabled; }
    public void setWaitingRoomEnabled(boolean waitingRoomEnabled) { this.waitingRoomEnabled = waitingRoomEnabled; }
    public boolean isRecordingEnabled() { return recordingEnabled; }
    public void setRecordingEnabled(boolean recordingEnabled) { this.recordingEnabled = recordingEnabled; }
}