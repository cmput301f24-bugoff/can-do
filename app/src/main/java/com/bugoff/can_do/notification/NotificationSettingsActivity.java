package com.bugoff.can_do.notification;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.bugoff.can_do.R;
import com.google.android.material.materialswitch.MaterialSwitch;
/**
 * Activity for managing notification settings.
 */
public class NotificationSettingsActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "NotificationPrefs";
    private static final String KEY_ORGANIZER_NOTIFICATIONS = "organizer_notifications_enabled";
    /**
     * Sets up the activity layout and initializes the notification settings.
     *
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_setting);

        // Set up back button
        ImageButton backButton = findViewById(R.id.notif_set_back_button);
        backButton.setOnClickListener(v -> finish());

        // Set up organizer notifications switch
        MaterialSwitch organizerSwitch = findViewById(R.id.switch_organizer_notifications);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Set initial state from saved preferences (default to true if not set)
        boolean organizerNotificationsEnabled = prefs.getBoolean(KEY_ORGANIZER_NOTIFICATIONS, true);
        organizerSwitch.setChecked(organizerNotificationsEnabled);

        // Handle toggle changes
        organizerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_ORGANIZER_NOTIFICATIONS, isChecked);
            editor.apply();
        });
    }

    // Static helper methods for other parts of the app to check notification settings
    public static boolean areOrganizerNotificationsEnabled(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_ORGANIZER_NOTIFICATIONS, true);
    }
}