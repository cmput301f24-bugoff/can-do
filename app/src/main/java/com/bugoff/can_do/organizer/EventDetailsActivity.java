package com.bugoff.can_do.organizer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.bugoff.can_do.R;
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.event.EventSelectedFragment;
import com.bugoff.can_do.event.EventViewModel;
import com.bugoff.can_do.event.EventViewModelFactory;
import com.bugoff.can_do.event.EventWaitlistFragment;
import com.bugoff.can_do.user.User;
import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class EventDetailsActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private TextView eventNameTextView; // Assume you have these TextViews in your layout
    private TextView eventDateTextView;
    private TextView eventDescriptionTextView;
    private TextView eventLocationTextView;
    private ListView entrantsListView;
    private ImageView eventImageView; // Add this variable
    private String eventLocation;
    private String eventDescription;
    private String eventName;
    private String eventId;
    private ImageView qrCodeImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventId = getIntent().getStringExtra("selected_event_id");
        setContentView(R.layout.activity_event_details_entrant); // Ensure layout name is correct

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        String eventId = getIntent().getStringExtra("selected_event_id");
        // Bind views
        eventNameTextView = findViewById(R.id.class_tile);
        eventDateTextView = findViewById(R.id.class_date);
        eventDescriptionTextView = findViewById(R.id.class_description);
        eventLocationTextView = findViewById(R.id.class_location);
        entrantsListView = findViewById(R.id.entrants_list);
        eventImageView = findViewById(R.id.event_image); // Initialize ImageView
        qrCodeImageView = findViewById(R.id.idIVQrcode);

        ImageButton backArrowButton = findViewById(R.id.back_arrow);
        ImageButton mapIconButton = findViewById(R.id.map_icon);
        ImageButton shareIconButton = findViewById(R.id.share_icon);
        ImageButton editGraphButton = findViewById(R.id.edit_graph);

        Button viewWatchListButton = findViewById(R.id.view_watch_list);
        Button viewSelectedButton = findViewById(R.id.view_selected_list);
        Button sendNotificationButton = findViewById(R.id.send_notification);

        Button joinWaitingListButton = findViewById(R.id.join_waiting_list);
        Button leaveWaitingListButton = findViewById(R.id.leave_waiting_list);

        // Set click listeners
        backArrowButton.setOnClickListener(v -> finish());

        mapIconButton.setOnClickListener(v -> openMapToLocation());

        shareIconButton.setOnClickListener(v -> shareEventDetails());

        EventViewModel viewModel = new ViewModelProvider(this, new EventViewModelFactory(eventId)).get(EventViewModel.class);
        String finalEventId = eventId;
        joinWaitingListButton.setOnClickListener(v -> {
            User currentUser = GlobalRepository.getLoggedInUser();
            if (currentUser == null) {
                throw new IllegalStateException("User not logged in");
            }

            String userId = currentUser.getId();

            // Add the user to the event's waiting list
            viewModel.addWaitingListEntrant(userId);

            // Add the event to the user's list of joined events
            currentUser.addEventJoined(finalEventId);
            Toast.makeText(this, "Successfully joined the waiting list.", Toast.LENGTH_SHORT).show();
        });

        leaveWaitingListButton.setOnClickListener(v -> {
            // Retrieve the current user
            User currentUser = GlobalRepository.getLoggedInUser();
            if (currentUser == null) {
                throw new IllegalStateException("User not logged in");
            }

            String userId = currentUser.getId();

            // Remove the user from the event's waiting list
            viewModel.removeWaitingListEntrant(userId);

            // Remove the event from the user's list of joined events
            currentUser.removeEventJoined(finalEventId);
            Toast.makeText(this, "Successfully left the waiting list.", Toast.LENGTH_SHORT).show();
        });

        editGraphButton.setOnClickListener(v -> {
            // Handle edit graph action
            Toast.makeText(this, "Edit Graph clicked", Toast.LENGTH_SHORT).show();
        });

        viewWatchListButton.setOnClickListener(v -> {
            Toast.makeText(this, "View Watch List clicked", Toast.LENGTH_SHORT).show();

            Fragment eventWaitlistFragment = new EventWaitlistFragment();

            // Pass eventId to the fragment
            Bundle args = new Bundle();
            args.putString("eventId", finalEventId);
            eventWaitlistFragment.setArguments(args);

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, eventWaitlistFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            Log.d("EventDetailsActivity", "View Watch List clicked");
        });

        viewSelectedButton.setOnClickListener(v -> {
            Toast.makeText(this, "View Selected clicked", Toast.LENGTH_SHORT).show();

            Fragment eventSelectedFragment = new EventSelectedFragment();

            // Pass eventId to the fragment
            Bundle args = new Bundle();
            args.putString("eventId", finalEventId);
            eventSelectedFragment.setArguments(args);

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, eventSelectedFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            Log.d("EventDetailsActivity", "View Selected List clicked");
        });

        sendNotificationButton.setOnClickListener(v -> {
            // Handle send notification action
            Toast.makeText(this, "Send Notification clicked", Toast.LENGTH_SHORT).show();
        });

        // Get event ID from intent
        eventId = getIntent().getStringExtra("selected_event_id");

        if (eventId != null) {
            fetchEventDetails(eventId);
        } else {
            Toast.makeText(this, "No Event ID provided", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchEventDetails(String eventId) {
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Fetch event details
                        eventName = documentSnapshot.getString("name");
                        eventDescription = documentSnapshot.getString("description");
                        eventLocation = documentSnapshot.getString("location");
                        Timestamp eventDateTimestamp = documentSnapshot.getTimestamp("eventStartDate");
                        String imageUrl = documentSnapshot.getString("imageUrl"); // Get imageUrl
                        String qrCodeText = documentSnapshot.getString("qrCodeHash");
                        generateQRCode(qrCodeText);

                        // Update TextViews with event details
                        eventNameTextView.setText(eventName != null ? eventName : "N/A");
                        eventDescriptionTextView.setText(eventDescription != null ? eventDescription : "No Description");
                        eventLocationTextView.setText("Location: " + (eventLocation != null ? eventLocation : "N/A"));

                        if (eventDateTimestamp != null) {
                            Date eventDate = eventDateTimestamp.toDate();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                            String formattedDate = dateFormat.format(eventDate);
                            eventDateTextView.setText("Date: " + formattedDate);
                        } else {
                            eventDateTextView.setText("Date: N/A");
                        }

                        // Load the image using Glide
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .into(eventImageView);
                        }

                        // Fetch and display the list of entrants
                        fetchEntrants();
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load event details", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchEntrants() {
        db.collection("events").document(eventId).collection("entrants")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> entrantNames = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        if (name != null) {
                            entrantNames.add(name);
                        }
                    }
                    // Display entrants in the ListView
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1, entrantNames);
                    entrantsListView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load entrants", Toast.LENGTH_SHORT).show();
                });
    }

    private void openMapToLocation() {
        if (eventLocation != null && !eventLocation.isEmpty()) {
            Uri geoLocation = Uri.parse("geo:0,0?q=" + Uri.encode(eventLocation));
            Intent intent = new Intent(Intent.ACTION_VIEW, geoLocation);
            intent.setPackage("com.google.android.apps.maps");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "No map application found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Event location not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareEventDetails() {
        String shareContent = "Check out this event: " + eventName + "\n"
                + "Date: " + eventDateTextView.getText().toString().replace("Date: ", "") + "\n"
                + "Location: " + eventLocation + "\n"
                + "Description: " + eventDescription;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
        startActivity(Intent.createChooser(shareIntent, "Share Event via"));
    }

    private void generateQRCode(String text)
    {
        BarcodeEncoder barcodeEncoder
                = new BarcodeEncoder();
        try {
            Bitmap bitmap = barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 400, 400);
            qrCodeImageView.setImageBitmap(bitmap); // Sets the Bitmap to ImageView
        }
        catch (WriterException e) {
            Log.e("TAG", e.toString());
        }
    }
}