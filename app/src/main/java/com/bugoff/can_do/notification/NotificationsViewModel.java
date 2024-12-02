package com.bugoff.can_do.notification;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for managing and observing notifications.
 * Connects to Firestore to fetch notifications for the logged-in user and updates the UI in real-time.
 */
public class NotificationsViewModel extends ViewModel {

    /**
     * Tag for logging purposes.
     */
    private static final String TAG = "NotificationsViewModel";

    /**
     * LiveData containing the list of notifications for the current user.
     */
    private final MutableLiveData<List<Notification>> notifications = new MutableLiveData<>(new ArrayList<>());

    /**
     * Instance of Firestore for database operations.
     */
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Listener for Firestore snapshot updates.
     */
    private ListenerRegistration notificationListener;

    /**
     * Retrieves a LiveData object containing the list of notifications for a specific user.
     * Sets up a listener to fetch notifications if one isn't already active.
     *
     * @param userId The ID of the user whose notifications are to be fetched.
     * @return A LiveData object containing the list of notifications.
     */
    public LiveData<List<Notification>> getNotifications(String userId) {
        if (notificationListener == null) {
            setupNotificationListener(userId);
        }
        return notifications;
    }

    /**
     * Sets up a Firestore listener to fetch and observe notifications for the given user ID.
     *
     * @param userId The ID of the user whose notifications are to be observed.
     */
    private void setupNotificationListener(String userId) {
        // Check if notifications are enabled for the current user
        if (!NotificationSettingsActivity.areOrganizerNotificationsEnabled(FirebaseFirestore.getInstance().getApp().getApplicationContext())) {
            notifications.setValue(new ArrayList<>());
            return;
        }

        // Query notifications where the user is in pendingRecipients
        Query query = db.collection("notifications")
                .whereArrayContains("pendingRecipients", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        notificationListener = query.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                Log.e(TAG, "Listen failed.", error);
                return;
            }

            if (snapshots != null && !snapshots.isEmpty()) {
                List<Notification> notificationList = new ArrayList<>();
                WriteBatch batch = db.batch(); // Batch for removing the user from pendingRecipients
                boolean hasBatchOperations = false;

                for (DocumentSnapshot doc : snapshots) {
                    Notification notification = documentToNotification(doc);
                    if (notification != null) {
                        notificationList.add(notification);

                        // Atomically remove the user from pendingRecipients
                        batch.update(doc.getReference(), "pendingRecipients",
                                com.google.firebase.firestore.FieldValue.arrayRemove(userId));

                        hasBatchOperations = true;
                    }
                }

                // Commit the batch if there are any operations
                if (hasBatchOperations) {
                    batch.commit().addOnFailureListener(e ->
                            Log.e(TAG, "Error updating notifications", e));
                }

                notifications.setValue(notificationList);
                Log.d(TAG, "Received notifications update: " + notificationList.size() + " notifications");
            } else {
                notifications.setValue(new ArrayList<>());
                Log.d(TAG, "No notifications found for user");
            }
        });
    }

    /**
     * Converts a Firestore document snapshot into a Notification object.
     *
     * @param doc The Firestore document snapshot to convert.
     * @return A Notification object, or {@code null} if conversion fails.
     */
    private Notification documentToNotification(DocumentSnapshot doc) {
        try {
            Notification notification = new Notification(
                    doc.getId(),
                    doc.getString("type"),
                    doc.getString("message"),
                    doc.getString("from"),
                    (List<String>) doc.get("pendingRecipients"),
                    doc.getString("event")
            );
            notification.setTimestamp(doc.getTimestamp("timestamp"));
            return notification;
        } catch (Exception e) {
            Log.e(TAG, "Error converting document to notification", e);
            return null;
        }
    }

    /**
     * Cleans up resources when the ViewModel is destroyed.
     * Removes the Firestore snapshot listener if it exists.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        if (notificationListener != null) {
            notificationListener.remove();
        }
    }
}
