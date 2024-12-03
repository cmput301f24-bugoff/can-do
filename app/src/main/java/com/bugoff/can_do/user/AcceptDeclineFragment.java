package com.bugoff.can_do.user;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
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

import com.bugoff.can_do.ImageUtils;
import com.bugoff.can_do.R;
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.event.EventViewModel;
import com.bugoff.can_do.event.EventViewModelFactory;
import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
/**
 * Fragment for accepting or declining an event invitation.
 */
public class AcceptDeclineFragment extends Fragment {
    private FirebaseFirestore db;
    private TextView eventNameTextView;
    private TextView eventDateTextView;
    private TextView eventDescriptionTextView;
    private TextView eventLocationTextView;
    private ImageView eventImageView;
    private String eventName;
    private String eventId;
    private String eventLocation;
    private static final String ARG_EVENT_ID = "selected_event_id";
    /**
     * Creates a new instance of AcceptDeclineFragment with the provided event ID.
     *
     * @param eventId ID of the event to accept or decline
     * @return A new instance of AcceptDeclineFragment
     */
    public static AcceptDeclineFragment newInstance(String eventId) {
        AcceptDeclineFragment fragment = new AcceptDeclineFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accept_decline, container, false);

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
        Button acceptInvitationButton = view.findViewById(R.id.accept_invitation);
        Button rejectInvitationButton = view.findViewById(R.id.reject_invitation);

        backArrowButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        EventViewModel viewModel = new ViewModelProvider(this, new EventViewModelFactory(eventId)).get(EventViewModel.class);
        acceptInvitationButton.setOnClickListener(v -> acceptInvitation(eventId));
        rejectInvitationButton.setOnClickListener(v -> rejectInvitation(eventId));

        if (eventId != null) {
            fetchEventDetails(eventId);
        } else {
            Toast.makeText(requireContext(), "No Event ID provided", Toast.LENGTH_SHORT).show();
        }

        return view;
    }
    /**
     * Fetches event details from Firestore and populates the UI.
     *
     * @param eventId ID of the event to fetch
     */
    private void fetchEventDetails(String eventId) {
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        eventName = documentSnapshot.getString("name");
                        String eventDescription = documentSnapshot.getString("description");
                        Timestamp eventDateTimestamp = documentSnapshot.getTimestamp("eventStartDate");

                        eventNameTextView.setText(eventName != null ? eventName : "N/A");
                        eventDescriptionTextView.setText(eventDescription != null ? eventDescription : "No Description");
                        GlobalRepository.getFacility(documentSnapshot.getString("facilityId")).addOnSuccessListener(facility -> {
                            eventLocation = facility.getAddress();
                            Log.d(TAG, "fetchEventDetails: " + eventLocation);
                            eventLocationTextView.setText("Address: " + (eventLocation != null ? eventLocation : "N/A"));
                        });

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

                        String base64Image = documentSnapshot.getString("base64Image");
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
                    } else {
                        Toast.makeText(requireContext(), "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load event details", Toast.LENGTH_SHORT).show();
                });
    }

    private void openMapToLocation() {
        Toast.makeText(requireContext(), "Map feature not implemented.", Toast.LENGTH_SHORT).show();
    }
    /**
     * Accepts an invitation to an event.
     *
     * @param eventId ID of the event to accept
     */
    private void acceptInvitation(String eventId) {
        String androidId = GlobalRepository.getLoggedInUser().getId();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("events").document(eventId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(eventRef);

            // Get current lists
            List<String> selectedEntrants = (List<String>) snapshot.get("selectedEntrants");
            List<String> enrolledEntrants = (List<String>) snapshot.get("enrolledEntrants");

            if (selectedEntrants != null && selectedEntrants.contains(androidId)) {
                selectedEntrants.remove(androidId);

                if (enrolledEntrants == null) {
                    enrolledEntrants = new ArrayList<>();
                }
                enrolledEntrants.add(androidId);

                // Update Firestore
                transaction.update(eventRef, "selectedEntrants", selectedEntrants);
                transaction.update(eventRef, "enrolledEntrants", enrolledEntrants);
            }

            return null;
        }).addOnSuccessListener(aVoid ->
                Toast.makeText(requireContext(), "You have accepted the invitation.", Toast.LENGTH_SHORT).show()
        ).addOnFailureListener(e ->
                Toast.makeText(requireContext(), "Error accepting invitation: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }
    /**
     * Rejects an invitation to an event.
     *
     * @param eventId ID of the event to reject
     */
    private void rejectInvitation(String eventId) {
        String androidId = GlobalRepository.getLoggedInUser().getId();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("events").document(eventId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(eventRef);

            // Get current lists
            List<String> selectedEntrants = (List<String>) snapshot.get("selectedEntrants");
            List<String> cancelledEntrants = (List<String>) snapshot.get("cancelledEntrants");

            if (selectedEntrants != null && selectedEntrants.contains(androidId)) {
                selectedEntrants.remove(androidId);

                if (cancelledEntrants == null) {
                    cancelledEntrants = new ArrayList<>();
                }
                cancelledEntrants.add(androidId);

                // Update Firestore
                transaction.update(eventRef, "selectedEntrants", selectedEntrants);
                transaction.update(eventRef, "cancelledEntrants", cancelledEntrants);
            }
            
            return null;
        }).addOnSuccessListener(aVoid ->
                Toast.makeText(requireContext(), "You have rejected the invitation.", Toast.LENGTH_SHORT).show()
        ).addOnFailureListener(e ->
                Toast.makeText(requireContext(), "Error rejecting invitation: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }


}
