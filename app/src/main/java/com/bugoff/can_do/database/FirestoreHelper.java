package com.bugoff.can_do.database;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * {@code FirestoreHelper} is a singleton class that provides a single instance of Firestore.
 */
public class FirestoreHelper {
    private static FirestoreHelper instance;
    private FirebaseFirestore db;

    // Want Singleton pattern
    private FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
    }

    // Get the single instance of FirestoreHelper
    public static synchronized FirestoreHelper getInstance() {
        if (instance == null) {
            instance = new FirestoreHelper();
        }
        return instance;
    }

    // Get Firestore instance
    public FirebaseFirestore getDb() {
        return db;
    }
}
