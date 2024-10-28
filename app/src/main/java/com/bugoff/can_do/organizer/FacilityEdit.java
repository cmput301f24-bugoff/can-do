package com.bugoff.can_do.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import com.bugoff.can_do.R;

public class FacilityEdit extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facility_edit);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());  // Closes the activity

        // Check if the user has a facility
        if (!userHasFacility()) {
            // Prompt user to create a facility
        }
    }

    private boolean userHasFacility() {
        // Implement logic to check if user already has a facility
        return false; // Placeholder
    }
}
