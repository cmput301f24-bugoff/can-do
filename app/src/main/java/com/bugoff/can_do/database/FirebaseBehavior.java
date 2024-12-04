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

/**
 * The {@code FirebaseBehavior} class implements the {@link DatabaseBehavior} interface and provides concrete methods
 * to interact with the Firebase Firestore database. It handles operations such as saving events,
 * attaching and detaching listeners, and retrieving or adding users, events, facilities, and notifications.
 */
public class FirebaseBehavior implements DatabaseBehavior {
    private final Map<Event, ListenerRegistration> eventListeners = new HashMap<>();
    private final FirebaseFirestore db;

    /**
     * Constructs a new {@code FirebaseBehavior} instance and initializes the Firebase Firestore database instance.
     */
    public FirebaseBehavior() {
        this.db = FirebaseFirestore.getInstance();
    }
    /**
     * Saves the specified {@link Event} object to the Firestore database under the "events" collection.
     *
     * @param event The {@link Event} object to be saved.
     */
    @Override
    public void saveEvent(Event event) {
        DocumentReference eventRef = db.collection("events").document(event.getId());
        eventRef.set(event.toMap())
                .addOnSuccessListener(aVoid -> Log.d("FirebaseBehavior", "Event saved successfully"))
                .addOnFailureListener(e -> Log.e("FirebaseBehavior", "Error saving event", e));
    }
    /**
     * Attaches a snapshot listener to the specified {@link Event} in the Firestore database.
     * The {@code onUpdate} runnable is executed whenever the event data is updated.
     *
     * @param event    The {@link Event} object to listen to.
     * @param onUpdate A {@link Runnable} to execute when the event is updated.
     */
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
    /**
     * Detaches the snapshot listener associated with the specified {@link Event}.
     *
     * @param event The {@link Event} object whose listener is to be detached.
     */
    @Override
    public void detachListener(Event event) {
        ListenerRegistration registration = eventListeners.remove(event);
        if (registration != null) {
            registration.remove();
        }
    }
    /**
     * Retrieves a {@link User} object from the Firestore database by the specified {@code userId}.
     *
     * @param userId The ID of the user to retrieve.
     * @return A {@link Task} representing the asynchronous retrieval operation. The task will complete successfully
     * with the {@link User} object, or fail with an {@link Exception} if the user is not found or an error occurs.
     */
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
    /**
     * Retrieves an {@link Event} object from the Firestore database by the specified {@code eventId}.
     * This method also retrieves the associated {@link Facility} for the event.
     *
     * @param eventId The ID of the event to retrieve.
     * @return A {@link Task} representing the asynchronous retrieval operation. The task will complete successfully
     * with the {@link Event} object, or fail with an {@link Exception} if the event is not found or an error occurs.
     */
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
    /**
     * Retrieves a {@link Facility} object from the Firestore database by the specified {@code facilityId}.
     *
     * @param facilityId The ID of the facility to retrieve.
     * @return A {@link Task} representing the asynchronous retrieval operation. The task will complete successfully
     * with the {@link Facility} object, or fail with an {@link Exception} if the facility is not found or an error occurs.
     */
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
    /**
     * Adds a {@link User} object to the Firestore database under the "users" collection.
     *
     * @param user The {@link User} object to add.
     * @return A {@link Task} representing the asynchronous operation of adding the user.
     */
    @Override
    public Task<Void> addUser(User user) {
        return db.collection("users").document(user.getId()).set(user.toMap());
    }
    /**
     * Adds an {@link Event} object to the Firestore database under the "events" collection.
     *
     * @param event The {@link Event} object to add.
     * @return A {@link Task} representing the asynchronous operation of adding the event.
     */
    @Override
    public Task<Void> addEvent(Event event) {
        return db.collection("events").document(event.getId()).set(event.toMap());
    }
    /**
     * Adds a {@link Facility} object to the Firestore database under the "facilities" collection.
     *
     * @param facility The {@link Facility} object to add.
     * @return A {@link Task} representing the asynchronous operation of adding the facility.
     */
    @Override
    public Task<Void> addFacility(Facility facility) {
        return db.collection("facilities").document(facility.getId()).set(facility.toMap());
    }
    /**
     * Adds a {@link Notification} object to the Firestore database under the "notifications" collection.
     *
     * @param notification The {@link Notification} object to add.
     */
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
