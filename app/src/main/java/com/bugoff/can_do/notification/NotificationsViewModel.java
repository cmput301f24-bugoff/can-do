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

public class NotificationsViewModel extends ViewModel {
    private static final String TAG = "NotificationsViewModel";
    private final MutableLiveData<List<Notification>> notifications = new MutableLiveData<>(new ArrayList<>());
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration notificationListener;

    public LiveData<List<Notification>> getNotifications(String userId) {
        if (notificationListener == null) {
            setupNotificationListener(userId);
        }
        return notifications;
    }

    private void setupNotificationListener(String userId) {
        // Query for notifications where the user is in pendingRecipients
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
                WriteBatch batch = db.batch(); // Batch for removing user from pendingRecipients
                boolean hasBatchOperations = false;

                for (DocumentSnapshot doc : snapshots) {
                    Notification notification = documentToNotification(doc);
                    if (notification != null) {
                        notificationList.add(notification);

                        // Use arrayRemove for atomic removal of the user from pendingRecipients
                        batch.update(doc.getReference(), "pendingRecipients",
                                com.google.firebase.firestore.FieldValue.arrayRemove(userId));

                        // Note: We don't delete empty notifications here.
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

    @Override
    protected void onCleared() {
        super.onCleared();
        if (notificationListener != null) {
            notificationListener.remove();
        }
    }
}