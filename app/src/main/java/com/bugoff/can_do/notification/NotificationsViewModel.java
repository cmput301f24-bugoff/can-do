package com.bugoff.can_do.notification;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationsViewModel extends ViewModel {
    private final MutableLiveData<List<Notification>> notifications = new MutableLiveData<>(new ArrayList<>());
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<List<Notification>> getNotifications(String userId) {
        if (notifications.getValue().isEmpty()) {
            fetchNotifications(userId);
        }
        return notifications;
    }

    private void fetchNotifications(String userId) {
        db.collection("notifications")
                .whereEqualTo("to", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Notification> notificationList = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Log.d(TAG, "Document data: " + document.getData());
                        Notification notification = new Notification(
                                document.getId(),
                                document.getString("type"),
                                document.getString("message"),
                                document.getString("from"),
                                document.getString("to"),
                                document.getString("event")
                        );
                        notificationList.add(notification);
                    }
                    notifications.setValue(notificationList);
                    Log.d(TAG, "fetchNotifications: " + notificationList.size() + " notifications fetched");

                    StringBuilder notificationContents = new StringBuilder("Notifications: ");
                    for (Notification notification : notificationList) {
                        notificationContents.append("\n").append(notification.getContent());
                    }
                    Log.d(TAG, notificationContents.toString());

                })
                .addOnFailureListener(e -> {
                    // Handle any error scenarios
                });
    }
}
