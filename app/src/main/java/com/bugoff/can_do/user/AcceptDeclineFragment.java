package com.bugoff.can_do.user;

import android.graphics.Bitmap;
import android.os.Bundle;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
/**
 * Fragment for accepting or declining an event invitation.
 */
public class AcceptDeclineFragment extends Fragment {
    private static final String TAG = "AcceptDeclineFragment";
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
        GlobalRepository.getEvent(eventId)
                .addOnSuccessListener(event -> {
                    if (event != null) {
                        eventName = event.getName();
                        String eventDescription = event.getDescription();
                        Date eventDate = event.getEventStartDate();

                        eventNameTextView.setText(eventName != null ? eventName : "N/A");
                        eventDescriptionTextView.setText(eventDescription != null ? eventDescription : "No Description");

                        if (event.getFacility() != null) {
                            eventLocation = event.getFacility().getAddress();
                            eventLocationTextView.setText("Address: " + (eventLocation != null ? eventLocation : "N/A"));
                        }

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
                    } else {
                        Toast.makeText(requireContext(), "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load event details", Toast.LENGTH_SHORT).show();
                });
    }
    /**
     * Accepts an invitation to an event.
     *
     * @param eventId ID of the event to accept
     */
    private void acceptInvitation(String eventId) {
        String userId = GlobalRepository.getLoggedInUser().getId();

        GlobalRepository.getEvent(eventId)
                .addOnSuccessListener(event -> {
                    if (event != null) {
                        List<String> selectedEntrants = event.getSelectedEntrants();

                        if (selectedEntrants.contains(userId)) {
                            // Remove from selected list
                            event.removeSelectedEntrant(userId);
                            // Add to enrolled list
                            event.enrollEntrant(userId);
                            // Update the event
                            event.setRemote();

                            Toast.makeText(requireContext(), "You have accepted the invitation.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error accepting invitation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    /**
     * Rejects an invitation to an event.
     *
     * @param eventId ID of the event to reject
     */
    private void rejectInvitation(String eventId) {
        String userId = GlobalRepository.getLoggedInUser().getId();

        GlobalRepository.getEvent(eventId)
                .addOnSuccessListener(event -> {
                    if (event != null) {
                        List<String> selectedEntrants = event.getSelectedEntrants();

                        if (selectedEntrants.contains(userId)) {
                            // Remove from selected list and add to cancelled list
                            event.removeSelectedEntrant(userId);

                            // Create new list for cancelled entrants
                            List<String> updatedCancelledEntrants = new ArrayList<>(event.getCancelledEntrants());
                            updatedCancelledEntrants.add(userId);

                            // Set the updated list of cancelled entrants
                            event.setCancelledEntrants(updatedCancelledEntrants);

                            // Update the event
                            event.setRemote();

                            Toast.makeText(requireContext(), "You have rejected the invitation.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error rejecting invitation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
