package com.bugoff.can_do.notification;

import com.google.firebase.Timestamp;
import java.util.List;

/**
 * Represents a notification in the "can-do" application.
 * This class includes information such as notification type, message, sender, recipients, associated event, and timestamp.
 */
public class Notification {

    /**
     * Unique identifier for the notification.
     */
    private String id;

    /**
     * Type of notification (e.g., info, warning, error).
     */
    private String type;

    /**
     * The content of the notification.
     */
    private String message;

    /**
     * Sender of the notification.
     */
    private String from;

    /**
     * List of recipients who have not yet received the notification.
     */
    private List<String> pendingRecipients;

    /**
     * Event associated with this notification.
     */
    private String event;

    /**
     * Timestamp of when the notification was created.
     */
    private Timestamp timestamp;

    /**
     * Default constructor required for Firestore.
     */
    public Notification() {
        // Required empty constructor for Firestore
    }

    /**
     * Constructs a Notification with the specified parameters.
     *
     * @param id                Unique identifier for the notification.
     * @param type              Type of notification (e.g., info, warning, error).
     * @param message           The content of the notification.
     * @param from              Sender of the notification.
     * @param pendingRecipients List of recipients who have not yet received the notification.
     * @param event             Event associated with this notification.
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

    /**
     * Returns the unique identifier of the notification.
     *
     * @return The unique identifier.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the type of the notification.
     *
     * @return The type of notification.
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the content of the notification.
     *
     * @return The content/message of the notification.
     */
    public String getContent() {
        return message;
    }

    /**
     * Returns the sender of the notification.
     *
     * @return The sender of the notification.
     */
    public String getFrom() {
        return from;
    }

    /**
     * Returns the list of recipients who have not yet received the notification.
     *
     * @return A list of pending recipients.
     */
    public List<String> getPendingRecipients() {
        return pendingRecipients;
    }

    /**
     * Returns the event associated with the notification.
     *
     * @return The associated event.
     */
    public String getEvent() {
        return event;
    }

    /**
     * Returns the timestamp when the notification was created.
     *
     * @return The timestamp of the notification.
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the unique identifier for the notification.
     *
     * @param id The unique identifier to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets the type of the notification.
     *
     * @param type The type to set (e.g., info, warning, error).
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Sets the content of the notification.
     *
     * @param message The content/message to set.
     */
    public void setContent(String message) {
        this.message = message;
    }

    /**
     * Sets the sender of the notification.
     *
     * @param from The sender to set.
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * Sets the list of recipients who have not yet received the notification.
     *
     * @param pendingRecipients The list of pending recipients to set.
     */
    public void setPendingRecipients(List<String> pendingRecipients) {
        this.pendingRecipients = pendingRecipients;
    }

    /**
     * Sets the event associated with the notification.
     *
     * @param event The associated event to set.
     */
    public void setEvent(String event) {
        this.event = event;
    }

    /**
     * Sets the timestamp for the notification.
     *
     * @param timestamp The timestamp to set.
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Removes a recipient from the list of pending recipients.
     *
     * @param userId The ID of the recipient to remove.
     */
    public void removeRecipient(String userId) {
        pendingRecipients.remove(userId);
    }

    /**
     * Checks if there are any pending recipients.
     *
     * @return {@code true} if there are pending recipients, {@code false} otherwise.
     */
    public boolean hasPendingRecipients() {
        return pendingRecipients != null && !pendingRecipients.isEmpty();
    }

    /**
     * Checks if a specific recipient is still pending.
     *
     * @param userId The ID of the recipient to check.
     * @return {@code true} if the recipient is pending, {@code false} otherwise.
     */
    public boolean isPendingFor(String userId) {
        return pendingRecipients != null && pendingRecipients.contains(userId);
    }
}
