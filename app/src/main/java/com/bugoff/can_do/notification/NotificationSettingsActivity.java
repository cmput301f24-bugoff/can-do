package com.bugoff.can_do.notification;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.bugoff.can_do.R;
import com.google.android.material.materialswitch.MaterialSwitch;

/**
 * Activity for managing notification settings in the "can-do" application.
 * Allows users to enable or disable notifications for organizers.
 */
public class NotificationSettingsActivity extends AppCompatActivity {

    /**
     * SharedPreferences file name for storing notification preferences.
     */
    private static final String PREFS_NAME = "NotificationPrefs";

    /**
     * Key used to store the state of organizer notifications.
     */
    private static final String KEY_ORGANIZER_NOTIFICATIONS = "organizer_notifications_enabled";

    /**
     * Called when the activity is first created. Sets up the UI components
     * and handles interaction with the organizer notifications switch.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down, this Bundle contains the data
     *                           it most recently supplied in {@link #onSaveInstanceState}.
     *                           Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_setting);

        // Set up the back button functionality
        ImageButton backButton = findViewById(R.id.notif_set_back_button);
        backButton.setOnClickListener(v -> finish());

        // Set up the organizer notifications switch
        MaterialSwitch organizerSwitch = findViewById(R.id.switch_organizer_notifications);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize the switch state based on stored preferences (default to true)
        boolean organizerNotificationsEnabled = prefs.getBoolean(KEY_ORGANIZER_NOTIFICATIONS, true);
        organizerSwitch.setChecked(organizerNotificationsEnabled);

        // Listen for changes in the switch state
        organizerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_ORGANIZER_NOTIFICATIONS, isChecked);
            editor.apply();
        });
    }

    /**
     * Checks if organizer notifications are enabled from SharedPreferences.
     *
     * @param context The context used to access SharedPreferences.
     * @return {@code true} if organizer notifications are enabled, {@code false} otherwise.
     */
    public static boolean areOrganizerNotificationsEnabled(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_ORGANIZER_NOTIFICATIONS, true);
    }
}
