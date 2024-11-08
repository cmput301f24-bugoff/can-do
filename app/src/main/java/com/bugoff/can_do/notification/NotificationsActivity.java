package com.bugoff.can_do.notification;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bugoff.can_do.R;
import com.bugoff.can_do.user.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to display a list of notifications for a user.
 */
public class NotificationsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private List<Notification> notificationList;

    /**
     * Called when the activity is starting. Initializes the activity and sets up UI elements.
     *
     * @param savedInstanceState if the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the most recent data; otherwise, it is null.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications); // Ensure this is the correct layout file name

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        notificationList = new ArrayList<>();

        // Find the back button and set its click listener to finish the activity
        ImageButton backButton = findViewById(R.id.notif_back_button);
        backButton.setOnClickListener(v -> finish()); // Closes the activity

        // Find the ListView in the layout
        ListView notifListView = findViewById(R.id.notif_list);

        // Get user ID (this can be passed via intent or any other method you choose)
        String userId = "USER_ID_HERE"; // Replace this with a method to get the current user ID

        // Fetch the user's notification list from Firestore
        fetchUserNotifications(userId, notifListView);
    }

    /**
     * Fetches the user's notifications from Firestore and updates the ListView.
     *
     * @param userId       the ID of the user whose notifications are to be fetched
     * @param notifListView the ListView to display the notifications
     */
    private void fetchUserNotifications(String userId, ListView notifListView) {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = new User(documentSnapshot);
                notificationList = user.getNotificationList();

                // Create and set the adapter with the retrieved notifications
                NotificationAdapter adapter = new NotificationAdapter(this, notificationList);
                notifListView.setAdapter(adapter);
            } else {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e("NotificationsActivity", "Error fetching user data", e);
            Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
        });
    }
}
