package com.bugoff.can_do.user;

import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bugoff.can_do.R;

public class EventDetailActivityEntrant extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details_entrant);

        // Retrieve TextView elements
        TextView eventNameTextView = findViewById(R.id.class_title);
        TextView eventDateTextView = findViewById(R.id.class_date);

        // Get data passed from HandleQRScan
        String eventName = getIntent().getStringExtra("event_name");
        String eventDate = getIntent().getStringExtra("event_date");

        // Set the TextViews with the event details
        if (eventName != null) {
            eventNameTextView.setText(eventName);
        } else {
            eventNameTextView.setText("Event Name not available");
        }

        if (eventDate != null) {
            eventDateTextView.setText(eventDate);
        } else {
            eventDateTextView.setText("Event Date not available");
        }
    }
}
