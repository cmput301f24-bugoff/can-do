package com.bugoff.can_do.notification;

import com.google.firebase.Timestamp;
import java.util.List;
/**
 * Represents a notification that can be sent to users.
 *
 * <p>This class encapsulates the details of a notification, including its ID, type, message content,
 * sender, list of pending recipients, associated event, and timestamp. It provides methods to manage
 * recipients and check notification status.</p>
 */
public class Notification {
    private String id;
    private String type;
    private String message;
    private String from;
    private List<String> pendingRecipients;
    private String event;
    private Timestamp timestamp;
    /**
     * Default constructor required for Firestore deserialization.
     */
    public Notification() {
        // Required empty constructor for Firestore
    }
    /**
     * Constructs a new {@code Notification} with the specified details.
     *
     * @param id                The unique identifier for the notification.
     * @param type              The type of the notification.
     * @param message           The message content of the notification.
     * @param from              The sender of the notification.
     * @param pendingRecipients The list of recipients who have not yet received the notification.
     * @param event             The associated event ID for the notification.
     */
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
    /**
     * Removes a recipient from the list of pending recipients.
     *
     * @param userId The ID of the recipient to remove.
     */
    public void removeRecipient(String userId) {
        pendingRecipients.remove(userId);
    }
    /**
     * Checks if there are any pending recipients for the notification.
     *
     * @return {@code true} if there are pending recipients, {@code false} otherwise.
     */
    public boolean hasPendingRecipients() {
        return pendingRecipients != null && !pendingRecipients.isEmpty();
    }
    /**
     * Checks if there are any pending recipients for the notification.
     *
     * @return {@code true} if there are pending recipients, {@code false} otherwise.
     */
    public boolean isPendingFor(String userId) {
        return pendingRecipients != null && pendingRecipients.contains(userId);
    }
}
