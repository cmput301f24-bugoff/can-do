package com.bugoff.can_do.organizer;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bugoff.can_do.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

public class EventDetailsActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private TextView eventNameTextView; // Assume you have these TextViews in your layout
    private TextView eventDateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details); // Ensure layout name is correct

        db = FirebaseFirestore.getInstance();
        eventNameTextView = findViewById(R.id.event_name_text_view);
        eventDateTextView = findViewById(R.id.event_date_text_view);

        String eventId = getIntent().getStringExtra("selected_event_id");

        if (eventId != null) {
            fetchEventDetails(eventId);
        }
    }

    private void fetchEventDetails(String eventId) {
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String eventName = documentSnapshot.getString("name");
                        Timestamp eventDate = documentSnapshot.getTimestamp("eventStartDate");
                        // Update your TextViews with the event details
                        eventNameTextView.setText(eventName);
                        eventDateTextView.setText(eventDate.toDate().toString());
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load event details", Toast.LENGTH_SHORT).show();
                });
    }
}
