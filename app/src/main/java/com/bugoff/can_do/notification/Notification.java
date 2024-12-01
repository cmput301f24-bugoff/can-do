package com.bugoff.can_do.notification;

import com.google.firebase.Timestamp;
import java.util.List;

public class Notification {
    private String id;
    private String type;
    private String message;
    private String from;
    private List<String> pendingRecipients;
    private String event;
    private Timestamp timestamp;

    public Notification() {
        // Required empty constructor for Firestore
    }

    public Notification(String id, String type, String message, String from, List<String> pendingRecipients, String event) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.from = from;
        this.pendingRecipients = pendingRecipients;
        this.event = event;
        this.timestamp = Timestamp.now();
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getContent() { return message; }
    public String getFrom() { return from; }
    public List<String> getPendingRecipients() { return pendingRecipients; }
    public String getEvent() { return event; }
    public Timestamp getTimestamp() { return timestamp; }

    public void setId(String id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setContent(String message) { this.message = message; }
    public void setFrom(String from) { this.from = from; }
    public void setPendingRecipients(List<String> pendingRecipients) { this.pendingRecipients = pendingRecipients; }
    public void setEvent(String event) { this.event = event; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    // Recipient management
    public void removeRecipient(String userId) {
        pendingRecipients.remove(userId);
    }

    public boolean hasPendingRecipients() {
        return pendingRecipients != null && !pendingRecipients.isEmpty();
    }

    public boolean isPendingFor(String userId) {
        return pendingRecipients != null && pendingRecipients.contains(userId);
    }
}
