package com.bugoff.can_do.database;

import android.util.Log;

import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.notification.Notification;
import com.bugoff.can_do.user.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;

public class FirebaseBehavior implements DatabaseBehavior {
    private final Map<Event, ListenerRegistration> eventListeners = new HashMap<>();
    private final FirebaseFirestore db;

    public FirebaseBehavior() {
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public void saveEvent(Event event) {
        DocumentReference eventRef = db.collection("events").document(event.getId());
        eventRef.set(event.toMap())
                .addOnSuccessListener(aVoid -> Log.d("FirebaseBehavior", "Event saved successfully"))
                .addOnFailureListener(e -> Log.e("FirebaseBehavior", "Error saving event", e));
    }

    @Override
    public void attachListener(Event event, Runnable onUpdate) {
        DocumentReference eventRef = db.collection("events").document(event.getId());
        ListenerRegistration registration = eventRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.e("FirebaseBehavior", "Listen failed", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                onUpdate.run();
            }
        });
        eventListeners.put(event, registration);
    }

    @Override
    public void detachListener(Event event) {
        ListenerRegistration registration = eventListeners.remove(event);
        if (registration != null) {
            registration.remove();
        }
    }

    @Override
    public Task<User> getUser(String userId) {
        TaskCompletionSource<User> taskCompletionSource = new TaskCompletionSource<>();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = new User(documentSnapshot);
                        taskCompletionSource.setResult(user);
                    } else {
                        taskCompletionSource.setException(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(taskCompletionSource::setException);
        return taskCompletionSource.getTask();
    }

    @Override
    public Task<Event> getEvent(String eventId) {
        TaskCompletionSource<Event> taskCompletionSource = new TaskCompletionSource<>();
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String facilityId = documentSnapshot.getString("facilityId");
                        getFacility(facilityId)
                                .addOnSuccessListener(facility -> {
                                    Event event = new Event(facility, documentSnapshot);
                                    taskCompletionSource.setResult(event);
                                })
                                .addOnFailureListener(taskCompletionSource::setException);
                    } else {
                        taskCompletionSource.setException(new Exception("Event not found"));
                    }
                })
                .addOnFailureListener(taskCompletionSource::setException);
        return taskCompletionSource.getTask();
    }

    @Override
    public Task<Facility> getFacility(String facilityId) {
        TaskCompletionSource<Facility> taskCompletionSource = new TaskCompletionSource<>();
        db.collection("facilities").document(facilityId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Facility facility = new Facility(documentSnapshot);
                        taskCompletionSource.setResult(facility);
                    } else {
                        taskCompletionSource.setException(new Exception("Facility not found"));
                    }
                })
                .addOnFailureListener(taskCompletionSource::setException);
        return taskCompletionSource.getTask();
    }

    @Override
    public Task<Void> addUser(User user) {
        return db.collection("users").document(user.getId()).set(user.toMap());
    }

    @Override
    public Task<Void> addEvent(Event event) {
        return db.collection("events").document(event.getId()).set(event.toMap());
    }

    @Override
    public Task<Void> addFacility(Facility facility) {
        return db.collection("facilities").document(facility.getId()).set(facility.toMap());
    }

    @Override
    public void addNotification(Notification notification) {
        Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("id", notification.getId());
        notificationMap.put("type", notification.getType());
        notificationMap.put("message", notification.getContent());
        notificationMap.put("from", notification.getFrom());
        notificationMap.put("pendingRecipients", notification.getPendingRecipients());
        notificationMap.put("event", notification.getEvent());

        FirestoreHelper.getInstance().getDb().collection("notifications")
                .document(notification.getId())
                .set(notificationMap);
    }
}
