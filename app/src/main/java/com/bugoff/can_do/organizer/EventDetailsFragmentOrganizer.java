package com.bugoff.can_do.organizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bugoff.can_do.ImageUtils;
import com.bugoff.can_do.R;
import com.bugoff.can_do.admin.AdminActivity;
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.database.NoOpDatabaseBehavior;
import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.event.EventCancelledFragment;
import com.bugoff.can_do.event.EventEnrolledFragment;
import com.bugoff.can_do.event.EventSelectedFragment;
import com.bugoff.can_do.event.EventWaitlistFragment;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.notification.SendNotificationFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment for displaying and managing event details for organizers.
 *
 * <p>This fragment allows organizers to view detailed information about an event, manage its
 * associated resources (e.g., QR codes, images, geolocation settings), and perform various
 * operations such as sharing event details, sending notifications, and viewing event participant lists.</p>
 */
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
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private static final String ARG_EVENT_ID = "selected_event_id";
    private androidx.appcompat.widget.SwitchCompat geolocationToggle;
    private NoOpDatabaseBehavior testBehavior;

    private static final String TAG = "EventDetailsFragmentOrg";
    /**
     * Creates a new instance of {@code EventDetailsFragmentOrganizer} for the specified event ID.
     *
     * @param eventId The ID of the event to manage.
     * @return A new instance of the fragment.
     */
    public static EventDetailsFragmentOrganizer newInstance(String eventId) {
        EventDetailsFragmentOrganizer fragment = new EventDetailsFragmentOrganizer();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize test behavior if in test mode
        if (GlobalRepository.isInTestMode()) {
            testBehavior = (NoOpDatabaseBehavior) GlobalRepository.getBehavior();
        }

        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
        }

        // Initialize image launchers
        setupImageLaunchers();
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
    /**
     * Sets up the buttons for the fragment.
     */
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
        Button viewCancelled = view.findViewById(R.id.view_cancelled_list);
        Button viewEnrolledButton = view.findViewById(R.id.view_enrolled_list);
        geolocationToggle = view.findViewById(R.id.geolocation_toggle);

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
            viewCancelled.setVisibility(View.GONE);
        } else {
            // Hide admin-only delete buttons
            deleteQrCodeButton.setVisibility(View.GONE);
            deleteFacilityButton.setVisibility(View.GONE);

            // Show organizer-specific buttons
            viewWatchListButton.setVisibility(View.VISIBLE);
            viewSelectedButton.setVisibility(View.VISIBLE);
            sendNotificationButton.setVisibility(View.VISIBLE);
            viewCancelled.setVisibility(View.VISIBLE);
        }

        // Setup click listeners
        backArrowButton.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        mapIconButton.setOnClickListener(v -> openMapToLocation());
        shareIconButton.setOnClickListener(v -> shareEventDetails());
        editGraphButton.setOnClickListener(v -> showImageSelectionDialog());

        viewWatchListButton.setOnClickListener(v -> showFragment(new EventWaitlistFragment(), "View Watch List clicked"));
        viewSelectedButton.setOnClickListener(v -> showFragment(new EventSelectedFragment(), "View Selected clicked"));
        viewCancelled.setOnClickListener(v -> showFragment(new EventCancelledFragment(), "View Cancelled List clicked"));
        viewEnrolledButton.setOnClickListener(v -> showFragment(new EventEnrolledFragment(), "View Enrolled clicked"));

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
    /**
     * Fetches the event details from Firestore and updates the UI.
     */
    private void fetchEventDetails(View rootView) {
        View progressBar = rootView.findViewById(R.id.progress_bar);
        View mainContent = rootView.findViewById(R.id.main_content);
        progressBar.setVisibility(View.VISIBLE);
        mainContent.setVisibility(View.GONE);

        GlobalRepository.getEvent(eventId)
                .addOnSuccessListener(event -> {
                    if (event != null) {
                        updateUI(event, rootView);
                        progressBar.setVisibility(View.GONE);
                        mainContent.setVisibility(View.VISIBLE);
                    } else {
                        handleError(rootView, "Event not found");
                    }
                })
                .addOnFailureListener(e -> handleError(rootView, "Failed to load event details: " + e.getMessage()));
    }
    /**
     * Updates the geolocation requirement for the event.
     */
    private void updateGeolocationRequirement(boolean required) {
        if (eventId == null || db == null) {
            Log.e(TAG, "Cannot update geolocation requirement: eventId or db is null");
            return;
        }

        View progressBar = requireView().findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        db.collection("events").document(eventId)
                .update("geolocationRequired", required)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    String message = required ?
                            "Geolocation requirement enabled" :
                            "Geolocation requirement disabled";
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error updating geolocation requirement", e);
                    Toast.makeText(requireContext(),
                            "Failed to update geolocation requirement",
                            Toast.LENGTH_SHORT).show();
                    // Revert the toggle if update fails
                    geolocationToggle.setChecked(!required);
                });
    }
    private void updateUI(Event event, View rootView) {
        eventName = event.getName();
        eventDescription = event.getDescription();
        Facility facility = event.getFacility();
        eventLocation = facility != null ? facility.getAddress() : "";

        eventNameTextView.setText(eventName != null ? eventName : "N/A");
        eventDescriptionTextView.setText(eventDescription != null ? eventDescription : "No Description");
        eventLocationTextView.setText("Address: " + (eventLocation != null ? eventLocation : "N/A"));

        Date eventDate = event.getEventStartDate();
        if (eventDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(eventDate);
            eventDateTextView.setText("Date: " + formattedDate);
        } else {
            eventDateTextView.setText("Date: N/A");
        }

        String base64Image = event.getBase64Image();
        if (base64Image != null) {
            Bitmap bitmap = ImageUtils.decodeBase64Image(base64Image);
            if (bitmap != null) {
                eventImageView.setImageBitmap(bitmap);
            } else {
                eventImageView.setVisibility(View.GONE);
            }
        } else {
            eventImageView.setVisibility(View.GONE);
        }

        String qrCodeText = event.getQrCodeHash();
        if (qrCodeText != null) {
            generateQRCode(qrCodeText);
        }

        Boolean geolocationRequired = event.getGeolocationRequired();
        geolocationToggle.setChecked(Boolean.TRUE.equals(geolocationRequired));
    }
    /**
     * Sets up the image launchers for selecting images from gallery or camera.
     */
    private void setupImageLaunchers() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        handleSelectedImage(selectedImageUri);
                    }
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            Bitmap photo = (Bitmap) extras.get("data");
                            if (photo != null) {
                                handleCapturedPhoto(photo);
                            }
                        }
                    }
                }
        );
    }
    /**
     * Shows a dialog for selecting an image source.
     */
    private void showImageSelectionDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Update Event Image")
                .setMessage("Choose image source")
                .setPositiveButton("Gallery", (dialog, which) -> {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryLauncher.launch(galleryIntent);
                })
                .setNegativeButton("Camera", (dialog, which) -> {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraLauncher.launch(cameraIntent);
                })
                .setNeutralButton("Remove", (dialog, which) -> {
                    removeEventImage();
                })
                .show();
    }
    /**
     * Handles the selected image from the gallery.
     */
    private void handleSelectedImage(Uri imageUri) {
        String base64Image = ImageUtils.compressAndEncodeImage(requireContext(), imageUri);
        if (base64Image != null) {
            updateEventImage(base64Image);
        } else {
            Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * Handles the captured photo from the camera.
     */
    private void handleCapturedPhoto(Bitmap photo) {
        String base64Image = ImageUtils.compressAndEncodeBitmap(photo);
        if (base64Image != null) {
            updateEventImage(base64Image);
        } else {
            Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * Updates the event image in Firestore.
     */
    private void updateEventImage(String base64Image) {
        if (eventId == null) return;

        View progressBar = requireView().findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        db.collection("events").document(eventId)
                .update("base64Image", base64Image)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    // Update the ImageView
                    Bitmap bitmap = ImageUtils.decodeBase64Image(base64Image);
                    if (bitmap != null) {
                        eventImageView.setImageBitmap(bitmap);
                        eventImageView.setVisibility(View.VISIBLE);
                    }
                    Toast.makeText(getContext(), "Image updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to update image", Toast.LENGTH_SHORT).show();
                });
    }
    /**
     * Removes the event image from Firestore.
     */
    private void removeEventImage() {
        if (eventId == null) return;

        View progressBar = requireView().findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        db.collection("events").document(eventId)
                .update("base64Image", null)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    eventImageView.setImageDrawable(null);
                    eventImageView.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Image removed successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to remove image", Toast.LENGTH_SHORT).show();
                });
    }
    /**
     * Handles errors by showing a toast message and hiding the loading spinner.
     */
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
    /**
     * Confirms and deletes the QR code hash from Firestore.
     */
    private void confirmAndDeleteQrHash() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete QR Code")
                .setMessage("Are you sure you want to delete the QR code for this event?")
                .setPositiveButton("Delete", (dialog, which) -> deleteQrHash())
                .setNegativeButton("Cancel", null)
                .show();
    }
    /**
     * Deletes the QR code hash from Firestore.
     */
    private void deleteQrHash() {
        if (eventId == null) return;

        if (GlobalRepository.isInTestMode()) {
            // Handle test mode
            Event event = testBehavior.getEvent(eventId).getResult();
            event.setQrCodeHash(null);
            testBehavior.saveEvent(event);
            Toast.makeText(requireContext(), "QR code deleted successfully", Toast.LENGTH_SHORT).show();
            if (qrCodeImageView != null) {
                qrCodeImageView.setImageDrawable(null);
            }
            return;
        }

        // Use GlobalRepository for production mode
        GlobalRepository.getEvent(eventId)
                .addOnSuccessListener(event -> {
                    event.setQrCodeHash(null);
                    event.setRemote();
                    Toast.makeText(requireContext(), "QR code deleted successfully", Toast.LENGTH_SHORT).show();
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
    /**
     * Confirms and deletes the facility and all associated events from Firestore.
     */
    private void confirmAndDeleteFacilityEvents() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Facility")
                .setMessage("Are you sure you want to delete this facility and ALL its associated events? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteFacilityEvents())
                .setNegativeButton("Cancel", null)
                .show();
    }
    /**
     * Deletes the facility and all associated events from Firestore.
     */
    private void deleteFacilityEvents() {
        if (eventId == null) return;

        if (GlobalRepository.isInTestMode()) {
            // Handle test mode
            Event event = testBehavior.getEvent(eventId).getResult();
            Facility facility = event.getFacility();
            facility.getEvents().clear();
            testBehavior.addFacility(facility);
            requireActivity().onBackPressed();
            return;
        }

        // For production mode, use GlobalRepository methods
        GlobalRepository.getEvent(eventId)
                .addOnSuccessListener(event -> {
                    Facility facility = event.getFacility();
                    // Clear facility events
                    facility.getEvents().clear();
                    facility.setRemote();
                    Toast.makeText(requireContext(),
                            "Facility and all associated events deleted successfully",
                            Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Failed to delete facility data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error in deletion", e);
                });
    }

    /**
     * Opens the map to the event location.
     */
    private void openMapToLocation() {
        Intent intent = new Intent(requireContext(), WaitingListMapActivity.class);
        intent.putExtra("EVENT_ID", eventId);
        startActivity(intent);
    }
    /**
     * Shares the event details via a share intent.
     */
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
    /**
     * Generates a QR code for the given text and sets it to the ImageView.
     */
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
    /**
     * Shows a fragment with the given fragment class and logs the message.
     */
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