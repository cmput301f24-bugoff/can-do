package com.bugoff.can_do.user;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bugoff.can_do.R;
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.event.EventViewModel;
import com.bugoff.can_do.event.EventViewModelFactory;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventDetailsFragmentEntrant extends Fragment {
    private FirebaseFirestore db;
    private TextView eventNameTextView;
    private TextView eventDateTextView;
    private TextView eventDescriptionTextView;
    private TextView eventLocationTextView;
    private ImageView eventImageView;
    private String eventLocation;
    private String eventDescription;
    private String eventName;
    private String eventId;
    private static final String ARG_EVENT_ID = "selected_event_id";

    public static EventDetailsFragmentEntrant newInstance(String eventId) {
        EventDetailsFragmentEntrant fragment = new EventDetailsFragmentEntrant();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_details_entrant, container, false);

        db = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
        }

        eventNameTextView = view.findViewById(R.id.class_tile);
        eventDateTextView = view.findViewById(R.id.class_date);
        eventDescriptionTextView = view.findViewById(R.id.class_description);
        eventLocationTextView = view.findViewById(R.id.class_location);
        eventImageView = view.findViewById(R.id.event_image);

        ImageButton backArrowButton = view.findViewById(R.id.back_arrow);
        ImageButton mapIconButton = view.findViewById(R.id.map_icon);
        ImageButton shareIconButton = view.findViewById(R.id.share_icon);
        Button joinWaitingListButton = view.findViewById(R.id.join_waiting_list);
        Button leaveWaitingListButton = view.findViewById(R.id.leave_waiting_list);

        backArrowButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        mapIconButton.setOnClickListener(v -> openMapToLocation());
        shareIconButton.setOnClickListener(v -> shareEventDetails());

        EventViewModel viewModel = new ViewModelProvider(this, new EventViewModelFactory(eventId)).get(EventViewModel.class);
        joinWaitingListButton.setOnClickListener(v -> joinWaitingList(viewModel));
        leaveWaitingListButton.setOnClickListener(v -> leaveWaitingList(viewModel));

        if (eventId != null) {
            fetchEventDetails(eventId);
        } else {
            Toast.makeText(requireContext(), "No Event ID provided", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void fetchEventDetails(String eventId) {
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        eventName = documentSnapshot.getString("name");
                        eventDescription = documentSnapshot.getString("description");
                        GlobalRepository.getFacility(documentSnapshot.getString("facilityId")).addOnSuccessListener(facility -> {
                            eventLocation = facility.getAddress();
                            Log.d(TAG, "fetchEventDetails: " + eventLocation);
                            eventLocationTextView.setText("Address: " + (eventLocation != null ? eventLocation : "N/A"));
                        });
                        Timestamp eventDateTimestamp = documentSnapshot.getTimestamp("eventStartDate");
                        String imageUrl = documentSnapshot.getString("imageUrl");

                        eventNameTextView.setText(eventName != null ? eventName : "N/A");
                        eventDescriptionTextView.setText(eventDescription != null ? eventDescription : "No Description");

                        if (eventDateTimestamp != null) {
                            Date eventDate = eventDateTimestamp.toDate();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                            String formattedDate = dateFormat.format(eventDate);
                            eventDateTextView.setText("Date: " + formattedDate);
                        } else {
                            eventDateTextView.setText("Date: N/A");
                        }

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this).load(imageUrl).into(eventImageView);
                        }
                    } else {
                        Toast.makeText(requireContext(), "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load event details", Toast.LENGTH_SHORT).show();
                });
    }

    private void openMapToLocation() {
        if (eventLocation != null && !eventLocation.isEmpty()) {
            Uri geoLocation = Uri.parse("geo:0,0?q=" + Uri.encode(eventLocation));
            Intent intent = new Intent(Intent.ACTION_VIEW, geoLocation);
            intent.setPackage("com.google.android.apps.maps");
            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "No map application found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "Event location not available", Toast.LENGTH_SHORT).show();
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

    public void joinWaitingList(EventViewModel viewModel) {
        User currentUser = GlobalRepository.getLoggedInUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not logged in");
        }

        // Check if the event exists and if it requires geolocation
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long maxParticipants = documentSnapshot.getLong("maxNumberOfParticipants"); // Maximum allowed participants
                        List<String> currentParticipants = (List<String>) documentSnapshot.get("waitingListEntrants"); // Current waiting list

                        if (currentParticipants != null && maxParticipants != null && currentParticipants.size() >= maxParticipants) {
                            // Show a dialog or toast to inform the user the waiting list is full
                            Toast.makeText(requireContext(), "The waiting list is full. You cannot join this event.", Toast.LENGTH_SHORT).show();
                        } else {
                            Boolean requiresGeolocation = documentSnapshot.getBoolean("geolocationRequired");
                            if (Boolean.TRUE.equals(requiresGeolocation)) {
                                // Display warning dialog if geolocation is required
                                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                        .setTitle("Geolocation Required")
                                        .setMessage("This event requires geolocation tracking. Do you want to proceed?")
                                        .setPositiveButton("Yes", (dialog, which) -> proceedWithJoining(viewModel, currentUser))
                                        .setNegativeButton("No", null)
                                        .show();
                            } else {
                                // Proceed directly if geolocation is not required
                                proceedWithJoining(viewModel, currentUser);
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to check event requirements", Toast.LENGTH_SHORT).show();
                });
    }


    private void proceedWithJoining(EventViewModel viewModel, User currentUser) {
        viewModel.addWaitingListEntrant(currentUser.getId());
        currentUser.addEventJoined(eventId);

        // Update Firestore with the user's joined events
        GlobalRepository.getUsersCollection().document(currentUser.getId())
                .update("eventsJoined", currentUser.getEventsJoined())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Successfully joined the waiting list.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error joining waiting list", Toast.LENGTH_SHORT).show();
                });
    }

    private void getLocationAndUpdateUser(EventViewModel viewModel, User currentUser) {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            currentUser.setLatitude(location.getLatitude());
                            currentUser.setLongitude(location.getLongitude());
                            Log.d("User", "Location updated: " + location.getLatitude() + ", " + location.getLongitude());
                        } else {
                            Log.d("User", "Location is null");
                        }
                        updateUserInFirestore(viewModel, currentUser); // Proceed regardless of location success
                    })
                    .addOnFailureListener(e -> {
                        Log.e("User", "Failed to get location", e);
                        updateUserInFirestore(viewModel, currentUser); // Proceed without location update
                    });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void updateUserInFirestore(EventViewModel viewModel, User currentUser) {
        viewModel.addWaitingListEntrant(currentUser.getId());
        currentUser.addEventJoined(eventId);
        currentUser.setRemote(); // Updates Firestore document with the user's data, including latitude and longitude
    }

    public void leaveWaitingList(EventViewModel viewModel) {
        User currentUser = GlobalRepository.getLoggedInUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not logged in");
        }

        viewModel.removeWaitingListEntrant(currentUser.getId());
        currentUser.removeEventJoined(eventId);
        Toast.makeText(requireContext(), "Successfully left the waiting list.", Toast.LENGTH_SHORT).show();
    }
}
