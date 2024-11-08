package com.bugoff.can_do.notification;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.bugoff.can_do.R;

/**
 * Activity for managing notification settings within the application.
 */
public class NotificationSettingsActivity extends AppCompatActivity {

    /**
     * Called when the activity is starting. Initializes the activity and sets up UI elements.
     *
     * @param savedInstanceState if the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the most recent data; otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_setting);

        // Find the back button and set its click listener to close the activity
        ImageButton backButton = findViewById(R.id.notif_set_back_button);
        backButton.setOnClickListener(v -> finish());  // Closes the activity

        // Your notification settings logic here
    }
}
