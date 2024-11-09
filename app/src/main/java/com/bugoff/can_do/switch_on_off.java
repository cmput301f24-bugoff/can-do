package com.bugoff.can_do;

import android.content.res.ColorStateList;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

/**
 * Activity for the notification settings screen. Allows users to toggle different types of notifications on and off.
 */
public class switch_on_off extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_setting);

        // Find the SwitchCompat views
        SwitchCompat switchPush = findViewById(R.id.switch_push_notifications);
        SwitchCompat switchOrganizer = findViewById(R.id.switch_organizer_notifications);
        SwitchCompat switchAdmin = findViewById(R.id.switch_admin_notifications);

        // Set listeners to dynamically change colors when the state changes
        switchPush.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateSwitchColors(switchPush, isChecked);
        });

        switchOrganizer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateSwitchColors(switchOrganizer, isChecked);
        });

        switchAdmin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateSwitchColors(switchAdmin, isChecked);
        });

        // Set the initial colors for each switch (based on initial state)
        updateSwitchColors(switchPush, switchPush.isChecked());
        updateSwitchColors(switchOrganizer, switchOrganizer.isChecked());
        updateSwitchColors(switchAdmin, switchAdmin.isChecked());
    }

    /**
     * Updates the colors of the SwitchCompat's thumb and track based on its state.
     *
     * @param switchCompat The SwitchCompat view to update
     * @param isChecked    The current checked state of the switch
     */
    private void updateSwitchColors(SwitchCompat switchCompat, boolean isChecked) {
        if (isChecked) {
            // Set colors for the "ON" state
            switchCompat.setThumbTintList(ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.white)));
            switchCompat.setTrackTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.purple)));
        } else {
            // Set colors for the "OFF" state
            switchCompat.setThumbTintList(ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.white)));
            switchCompat.setTrackTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.grey)));
        }
    }
}
