package com.bugoff.can_do.notification;

/**
 * Represents a notification within the application.
 * Each notification has an ID, a type, and content.
 */
public class Notification {
    private String id; // Unique identifier for the notification
    private String type; // Can be "message", "selected", "rejected"
    private String content; // The content of the notification

    /**
     * Default constructor needed for Firestore deserialization.
     */
    public Notification() {
        // Default constructor
    }

    /**
     * Constructs a new Notification with the given id, type, and content.
     *
     * @param id      the unique identifier for the notification
     * @param type    the type of the notification (e.g., "message", "selected", "rejected")
     * @param content the content of the notification
     */
    public Notification(String id, String type, String content) {
        this.id = id;
        this.type = type;
        this.content = content;
    }

    /**
     * Gets the unique identifier for the notification.
     *
     * @return the notification ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier for the notification.
     *
     * @param id the new notification ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the type of the notification.
     *
     * @return the type of the notification
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of the notification.
     *
     * @param type the new type of the notification
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the content of the notification.
     *
     * @return the content of the notification
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content of the notification.
     *
     * @param content the new content of the notification
     */
    public void setContent(String content) {
        this.content = content;
    }
}
