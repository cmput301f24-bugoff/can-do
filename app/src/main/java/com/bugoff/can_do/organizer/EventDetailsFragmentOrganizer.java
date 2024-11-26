package com.bugoff.can_do.organizer;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bugoff.can_do.R;
import com.bugoff.can_do.admin.AdminActivity;
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.event.EventSelectedFragment;
import com.bugoff.can_do.event.EventWaitlistFragment;
import com.bugoff.can_do.notification.SendNotificationFragment;
import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventDetailsFragmentOrganizer extends Fragment {
    private FirebaseFirestore db;
    private TextView eventNameTextView;
    private TextView eventDateTextView;
    private TextView eventDescriptionTextView;
    private TextView eventLocationTextView;
    private ListView entrantsListView;
    private ImageView eventImageView;
    private String eventLocation;
    private String eventDescription;
    private String eventName;
    private String eventId;
    private ImageView qrCodeImageView;
    private static final String ARG_EVENT_ID = "selected_event_id";

    private static final String TAG = "EventDetailsFragmentOrg";

    public static EventDetailsFragmentOrganizer newInstance(String eventId) {
        EventDetailsFragmentOrganizer fragment = new EventDetailsFragmentOrganizer();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_details_organizer, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
        }

        // Bind views
        eventNameTextView = view.findViewById(R.id.class_tile);
        eventDateTextView = view.findViewById(R.id.class_date);
        eventDescriptionTextView = view.findViewById(R.id.class_description);
        eventLocationTextView = view.findViewById(R.id.class_location);
        eventImageView = view.findViewById(R.id.event_image);
        qrCodeImageView = view.findViewById(R.id.idIVQrcode);

        View progressBar = view.findViewById(R.id.progress_bar);
        View mainContent = view.findViewById(R.id.main_content);

        // Show loading state initially
        progressBar.setVisibility(View.VISIBLE);
        mainContent.setVisibility(View.GONE);

        // Set up buttons
        setupButtons(view);

        // Now fetch the event details
        if (eventId != null) {
            fetchEventDetails(view);
        } else {
            handleError(view, "No event ID provided");
        }

        return view;
    }

    private void setupButtons(View view) {
        ImageButton backArrowButton = view.findViewById(R.id.back_arrow);
        ImageButton mapIconButton = view.findViewById(R.id.map_icon);
        ImageButton shareIconButton = view.findViewById(R.id.share_icon);
        ImageButton editGraphButton = view.findViewById(R.id.edit_graph);
        ImageButton deleteQrCodeButton = view.findViewById(R.id.delete_qr_code_button);
        ImageButton deleteFacilityButton = view.findViewById(R.id.delete_facility_button);
        Button viewWatchListButton = view.findViewById(R.id.view_watch_list);
        Button viewSelectedButton = view.findViewById(R.id.view_selected_list);
        Button sendNotificationButton = view.findViewById(R.id.send_notification);

        // Check if we're in admin view
        boolean isFromAdmin = getActivity() instanceof AdminActivity;

        // Set visibility based on admin view
        if (isFromAdmin) {
            // Show admin-only delete buttons
            deleteQrCodeButton.setVisibility(View.VISIBLE);
            deleteFacilityButton.setVisibility(View.VISIBLE);

            // Hide organizer-specific buttons
            viewWatchListButton.setVisibility(View.GONE);
            viewSelectedButton.setVisibility(View.GONE);
            sendNotificationButton.setVisibility(View.GONE);
        } else {
            // Hide admin-only delete buttons
            deleteQrCodeButton.setVisibility(View.GONE);
            deleteFacilityButton.setVisibility(View.GONE);

            // Show organizer-specific buttons
            viewWatchListButton.setVisibility(View.VISIBLE);
            viewSelectedButton.setVisibility(View.VISIBLE);
            sendNotificationButton.setVisibility(View.VISIBLE);
        }

        // Setup click listeners
        backArrowButton.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        mapIconButton.setOnClickListener(v -> openMapToLocation());
        shareIconButton.setOnClickListener(v -> shareEventDetails());
        editGraphButton.setOnClickListener(v -> Toast.makeText(requireContext(), "Edit Graph clicked", Toast.LENGTH_SHORT).show());

        viewWatchListButton.setOnClickListener(v -> showFragment(new EventWaitlistFragment(), "View Watch List clicked"));
        viewSelectedButton.setOnClickListener(v -> showFragment(new EventSelectedFragment(), "View Selected clicked"));

        sendNotificationButton.setOnClickListener(v -> {
            SendNotificationFragment fragment = new SendNotificationFragment();
            Bundle args = new Bundle();
            args.putString("eventId", eventId);
            fragment.setArguments(args);
            showFragment(fragment, "Send Notification clicked");
        });

        deleteQrCodeButton.setOnClickListener(v -> confirmAndDeleteQrHash());
        deleteFacilityButton.setOnClickListener(v -> confirmAndDeleteFacilityEvents());
    }

    private void fetchEventDetails(View rootView) {
        View progressBar = rootView.findViewById(R.id.progress_bar);
        View mainContent = rootView.findViewById(R.id.main_content);

        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get all the basic event details first
                        eventName = documentSnapshot.getString("name");
                        eventDescription = documentSnapshot.getString("description");
                        Timestamp eventDateTimestamp = documentSnapshot.getTimestamp("eventStartDate");
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        String qrCodeText = documentSnapshot.getString("qrCodeHash");

                        // Update UI with the data we have
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

                        // Handle image
                        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equals("null")) {
                            eventImageView.setVisibility(View.VISIBLE);
                            Glide.with(this)
                                    .load(imageUrl)
                                    .error(R.drawable.ic_launcher_background)
                                    .into(eventImageView);
                        } else {
                            eventImageView.setVisibility(View.GONE);
                        }

                        // Generate QR code if we have the hash
                        if (qrCodeText != null) {
                            generateQRCode(qrCodeText);
                        }

                        // Now get the facility details
                        String facilityId = documentSnapshot.getString("facilityId");
                        if (facilityId != null) {
                            GlobalRepository.getFacility(facilityId)
                                    .addOnSuccessListener(facility -> {
                                        if (facility != null) {
                                            eventLocation = facility.getAddress();
                                            eventLocationTextView.setText("Address: " + (eventLocation != null ? eventLocation : "N/A"));

                                            // Now that we have everything, show the content
                                            progressBar.setVisibility(View.GONE);
                                            mainContent.setVisibility(View.VISIBLE);
                                        } else {
                                            handleError(rootView, "Facility not found");
                                        }
                                    })
                                    .addOnFailureListener(e -> handleError(rootView, "Failed to load facility details"));
                        } else {
                            handleError(rootView, "No facility ID found for event");
                        }
                    } else {
                        handleError(rootView, "Event not found");
                    }
                })
                .addOnFailureListener(e -> handleError(rootView, "Failed to load event details: " + e.getMessage()));
    }

    private void handleError(View rootView, String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            // Hide loading and show error state or empty content
            View progressBar = rootView.findViewById(R.id.progress_bar);
            View mainContent = rootView.findViewById(R.id.main_content);
            progressBar.setVisibility(View.GONE);
            mainContent.setVisibility(View.VISIBLE);
        }
    }

    private void confirmAndDeleteQrHash() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete QR Code")
                .setMessage("Are you sure you want to delete the QR code for this event?")
                .setPositiveButton("Delete", (dialog, which) -> deleteQrHash())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteQrHash() {
        if (eventId == null) return;

        db.collection("events").document(eventId)
                .update("qrCodeHash", null)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "QR code deleted successfully", Toast.LENGTH_SHORT).show();
                    // Refresh QR code view
                    if (qrCodeImageView != null) {
                        qrCodeImageView.setImageDrawable(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to delete QR code: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error deleting QR code", e);
                });
    }

    private void confirmAndDeleteFacilityEvents() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Facility")
                .setMessage("Are you sure you want to delete this facility and ALL its associated events? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteFacilityEvents())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteFacilityEvents() {
        if (eventId == null) return;

        // First, get the facility ID and owner ID of the current event
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String facilityId = documentSnapshot.getString("facilityId");
                    if (facilityId == null) {
                        Toast.makeText(requireContext(), "Facility ID not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Get the facility document to find its owner
                    db.collection("facilities").document(facilityId)
                            .get()
                            .addOnSuccessListener(facilityDoc -> {
                                String ownerId = facilityDoc.getString("ownerId");

                                // Query all events with this facility ID
                                db.collection("events")
                                        .whereEqualTo("facilityId", facilityId)
                                        .get()
                                        .addOnSuccessListener(querySnapshot -> {
                                            // Create a batch for all operations
                                            WriteBatch batch = db.batch();

                                            // Delete all events
                                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                                batch.delete(db.collection("events").document(doc.getId()));
                                            }

                                            // Delete the facility document
                                            batch.delete(db.collection("facilities").document(facilityId));

                                            // Execute the batch
                                            batch.commit()
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(requireContext(),
                                                                "Facility and all associated events deleted successfully",
                                                                Toast.LENGTH_SHORT).show();
                                                        // Return to previous screen
                                                        requireActivity().onBackPressed();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(requireContext(),
                                                                "Failed to delete facility data: " + e.getMessage(),
                                                                Toast.LENGTH_SHORT).show();
                                                        Log.e(TAG, "Error in batch deletion", e);
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(requireContext(),
                                                    "Failed to query facility events: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                            Log.e(TAG, "Error querying facility events", e);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(),
                                        "Failed to get facility details: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Error getting facility details", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Failed to get event details: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error getting event details", e);
                });
    }

    private void openMapToLocation() {
        Intent intent = new Intent(requireContext(), WaitingListMapActivity.class);
        intent.putExtra("EVENT_ID", eventId);
        startActivity(intent);
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

    private void generateQRCode(String text) {
        BarcodeEncoder barcodeEncoder
                = new BarcodeEncoder();
        try {
            Bitmap bitmap = barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 400, 400);
            qrCodeImageView.setImageBitmap(bitmap); // Sets the Bitmap to ImageView
        } catch (WriterException e) {
            Log.e("TAG", e.toString());
        }
    }

    private void showFragment(Fragment fragment, String logMessage) {
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);

        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        Log.d("EventDetailsFragmentOrganizer", logMessage);
    }
}
