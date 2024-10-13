package com.bugoff.can_do;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private FirebaseFirestore db;
    private CollectionReference usersCollection;

    // Constructor to initialize Firestore
    public UserRepository() {
        db = FirestoreHelper.getInstance().getDb();
        usersCollection = db.collection("users");
    }

    // Add a user to Firestore
    public void addUser(@NonNull User user,
                        OnSuccessListener<Void> onSuccess,
                        OnFailureListener onFailure) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("androidId", user.getAndroidId());
        userMap.put("name", user.getName());
        userMap.put("isAdmin", user.getIsAdmin());

        usersCollection.document(user.getAndroidId())
                .set(userMap)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // Get a user from Firestore by Android ID
    public void getUser(String androidId,
                        OnSuccessListener<User> onSuccess,
                        OnFailureListener onFailure) {
        usersCollection.document(androidId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        Boolean isAdmin = documentSnapshot.getBoolean("isAdmin");
                        Facility facility = new Facility("Facility Name", "Facility Address");
                        User user = new User(androidId, name, isAdmin, facility);
                        onSuccess.onSuccess(user);
                    } else {
                        onFailure.onFailure(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(onFailure);
    }
}
