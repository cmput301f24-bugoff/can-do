package com.bugoff.can_do.notification;

/**
 * Represents a notification that can be sent to a user.
 */
public class Notification {
    private String id; // Unique identifier for the notification
    private String type; // Can be "message", "selected", "rejected"
    private String content; // The content of the notification

    public Notification() {
        // Default constructor needed for Firestore deserialization
    }

    public Notification(String id, String type, String content) {
        this.id = id;
        this.type = type;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
