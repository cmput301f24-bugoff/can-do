package com.bugoff.can_do;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.event.EventAdapter;
import com.bugoff.can_do.notification.NotificationSettingsActivity;
import com.bugoff.can_do.notification.NotificationsFragment;
import com.bugoff.can_do.user.AcceptDeclineFragment;
import com.bugoff.can_do.user.EventDetailsFragmentEntrant;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fragment for the Home screen.
 */
public class HomeActivity extends Fragment {

    private TextView defaultSubtitle, getStartedText;
    private ImageView arrowDown;
    private RecyclerView eventsListView;
    private EventAdapter eventsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_screen, container, false);

        // Initialize UI components
        defaultSubtitle = view.findViewById(R.id.hs_default_subtitle);
        getStartedText = view.findViewById(R.id.get_started_text);
        arrowDown = view.findViewById(R.id.arrow_down);
        eventsListView = view.findViewById(R.id.hs_events_list);

        eventsAdapter = new EventAdapter(new ArrayList<>(), false, false, null, event -> handleEventClick(event), true);
//        eventsAdapter.setOnItemClickListener(this);
        eventsListView.setAdapter(eventsAdapter);
        eventsListView.setLayoutManager(new LinearLayoutManager(getContext()));

        String userId = GlobalRepository.getLoggedInUser().getId();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDoc = db.collection("users").document(userId);

        userDoc.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> eventsJoined = (List<String>) documentSnapshot.get("eventsJoined");
                if (eventsJoined != null && !eventsJoined.isEmpty()) {
                    // Hide default views
                    defaultSubtitle.setVisibility(View.GONE);
                    getStartedText.setVisibility(View.GONE);
                    arrowDown.setVisibility(View.GONE);


                    // Show events
                    eventsListView.setVisibility(View.VISIBLE);
                    Log.d(TAG, "test: made it here");
                    fetchEventDetails(eventsJoined);
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to load events.", Toast.LENGTH_SHORT).show();
        });

        // Functionality of notifications button on HomeScreen
        view.findViewById(R.id.notif_hs_button).setOnClickListener(v -> {
            NotificationsFragment notificationsFragment = NotificationsFragment.newInstance(userId);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, notificationsFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Functionality of settings button on HomeScreen
        view.findViewById(R.id.settings_hs_button).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationSettingsActivity.class);
            startActivity(intent);
        });


        return view;
    }

    private void handleEventClick(Event event) {
        String currentUserId = GlobalRepository.getLoggedInUser().getId();
        List<String> waitlist = event.getWaitingListEntrants();
        List<String> selectedParticipants = event.getSelectedEntrants();
        List<String> enrolledParticipants = event.getEnrolledEntrants();

        if (waitlist.contains(currentUserId)) {
            // User is in the waitlist
            Toast.makeText(getContext(), "Navigating to event details (waitlist)...", Toast.LENGTH_SHORT).show();
            navigateToEventDetails(event);
        } else if (selectedParticipants.contains(currentUserId)) {
            // User has been selected; show accept/decline fragment
            Toast.makeText(getContext(), "Navigating to accept/decline fragment...", Toast.LENGTH_SHORT).show();
            navigateToAcceptDecline(event);
        } else if (enrolledParticipants.contains(currentUserId)) {
            // User is enrolled
            Toast.makeText(getContext(), "Navigating to event details (enrolled)...", Toast.LENGTH_SHORT).show();
            navigateToEventDetails(event);
        } else {
            // Default fallback
            Toast.makeText(getContext(), "Unknown status for event.", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToEventDetails(Event event) {
        // Replace with navigation logic to event details screen
        EventDetailsFragmentEntrant fragment = new EventDetailsFragmentEntrant();
        Bundle args = new Bundle();
        args.putString("event_name", event.getName());
        args.putString("event_date", event.getEventStartDate().toString());
        args.putString("selected_event_id", event.getId()); // Pass eventId explicitly for further use
        fragment.setArguments(args);

        // Replace the current fragment with EventDetailsFragment
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment); // Replace 'fragment_container' with your actual container ID
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void navigateToAcceptDecline(Event event) {
        // Create an instance of the AcceptDeclineFragment with the event data
        AcceptDeclineFragment fragment = AcceptDeclineFragment.newInstance(event.getId());

        // Navigate to the AcceptDeclineFragment
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment) // Replace with the new fragment
                .addToBackStack(null) // Add to the back stack for navigation
                .commit();
    }

    private void fetchEventDetails(List<String> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            eventsAdapter.setEventList(new ArrayList<>());
            return;
        }

        List<Event> events = new ArrayList<>();
        final int totalEvents = eventIds.size();
        final AtomicInteger processedEvents = new AtomicInteger(0);

        for (String eventId : eventIds) {
            GlobalRepository.getEvent(eventId)
                    .addOnSuccessListener(event -> {
                        if (event != null) {
                            events.add(event);
                        } else {
                            Log.w(TAG, "Event was null for ID: " + eventId);
                        }
                        updateAdapterIfComplete(events, totalEvents, processedEvents.incrementAndGet());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to fetch event with ID: " + eventId, e);
                        updateAdapterIfComplete(events, totalEvents, processedEvents.incrementAndGet());
                    });
        }
    }

    private void updateAdapterIfComplete(List<Event> events, int totalEvents, int processedEvents) {
        if (processedEvents == totalEvents) {
            if (events.isEmpty()) {
                // All events failed to load or were null
                defaultSubtitle.setVisibility(View.VISIBLE);
                getStartedText.setVisibility(View.VISIBLE);
                arrowDown.setVisibility(View.VISIBLE);
                eventsListView.setVisibility(View.GONE);
            } else {
                // Sort events by date if needed
                defaultSubtitle.setVisibility(View.GONE);
                getStartedText.setVisibility(View.GONE);
                arrowDown.setVisibility(View.GONE);
                eventsListView.setVisibility(View.VISIBLE);
                eventsAdapter.setEventList(events);
            }
        }
    }
}
