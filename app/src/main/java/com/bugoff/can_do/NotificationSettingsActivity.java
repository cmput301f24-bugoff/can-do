package com.bugoff.can_do;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class NotificationSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_setting);
        ImageButton backButton = findViewById(R.id.notif_set_back_button);
        backButton.setOnClickListener(v -> finish());  // Closes the activity
        // Your notification settings logic here
    }

}
