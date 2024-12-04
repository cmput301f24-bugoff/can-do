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
import com.bugoff.can_do.database.NoOpDatabaseBehavior;
import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.event.EventAdapter;
import com.bugoff.can_do.notification.NotificationSettingsActivity;
import com.bugoff.can_do.notification.NotificationsFragment;
import com.bugoff.can_do.user.AcceptDeclineFragment;
import com.bugoff.can_do.user.EventDetailsFragmentEntrant;
import com.bugoff.can_do.user.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
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
        eventsListView.setAdapter(eventsAdapter);
        eventsListView.setLayoutManager(new LinearLayoutManager(getContext()));

        User currentUser = GlobalRepository.getLoggedInUser();
        String userId = currentUser.getId();
        Log.d(TAG, "HomeActivity starting for user: " + userId);
        Log.d(TAG, "User events joined: " + currentUser.getEventsJoined());

        if (GlobalRepository.isInTestMode()) {
            User user = GlobalRepository.getLoggedInUser();
            Log.d(TAG, "Test Mode - Current user: " + user.getId());
            Log.d(TAG, "Test Mode - Checking events from Repository");

            List<String> eventsJoined = user.getEventsJoined();
            Log.d(TAG, "Test Mode - Events joined from user: " + eventsJoined);

            // Double check against test behavior
            NoOpDatabaseBehavior behavior = (NoOpDatabaseBehavior) GlobalRepository.getBehavior();
            User storedUser = behavior.getUser(user.getId()).getResult();
            if (storedUser != null) {
                Log.d(TAG, "Test Mode - Events joined from storage: " + storedUser.getEventsJoined());
                // Use the stored user's events if available
                eventsJoined = storedUser.getEventsJoined();
            }

            if (eventsJoined != null && !eventsJoined.isEmpty()) {
                // Get events from test behavior
                List<Event> validEvents = new ArrayList<>();
                for (String eventId : eventsJoined) {
                    try {
                        Log.d(TAG, "Test Mode - Fetching event: " + eventId);
                        Event event = behavior.getEvent(eventId).getResult();
                        if (event != null) {
                            Log.d(TAG, "Test Mode - Found event: " + event.getName());
                            validEvents.add(event);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Test Mode - Error fetching event: " + eventId, e);
                    }
                }

                if (!validEvents.isEmpty()) {
                    Log.d(TAG, "Test Mode - Setting " + validEvents.size() + " events to adapter");
                    eventsAdapter.setEventList(validEvents);
                    defaultSubtitle.setVisibility(View.GONE);
                    getStartedText.setVisibility(View.GONE);
                    arrowDown.setVisibility(View.GONE);
                    eventsListView.setVisibility(View.VISIBLE);
                } else {
                    Log.d(TAG, "Test Mode - No valid events found");
                    showEmptyState();
                }
            } else {
                Log.d(TAG, "Test Mode - No events joined");
                showEmptyState();
            }
        } else {
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
        }

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
    /**
     * Handles the click event for an event.
     *
     * @param event The event that was clicked.
     */
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
            // Default fallback for just viewing
            Toast.makeText(getContext(), "Unknown status for event.", Toast.LENGTH_SHORT).show();
            navigateToEventDetails(event);
        }
    }
    /**
     * Navigates to the event details screen for the given event.
     *
     * @param event The event to navigate to.
     */
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
    /**
     * Navigates to the AcceptDeclineFragment for the given event.
     *
     * @param event The event to navigate to.
     */
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
    /**
     * Fetches the details for the given list of event IDs.
     *
     * @param eventIds The list of event IDs to fetch.
     */
    private void fetchEventDetails(List<String> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            Log.d(TAG, "fetchEventDetails - No event IDs to fetch");
            showEmptyState();
            return;
        }

        Log.d(TAG, "fetchEventDetails - Starting to fetch " + eventIds.size() + " events");
        List<Event> validEvents = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger processedCount = new AtomicInteger(0);

        for (String eventId : eventIds) {
            if (eventId == null || eventId.trim().isEmpty()) {
                Log.w(TAG, "fetchEventDetails - Skipping null or empty event ID");
                if (processedCount.incrementAndGet() == eventIds.size()) {
                    updateUI(validEvents);
                }
                continue;
            }

            Log.d(TAG, "fetchEventDetails - Fetching event: " + eventId);
            GlobalRepository.getEvent(eventId)
                    .addOnSuccessListener(event -> {
                        if (event != null) {
                            Log.d(TAG, "fetchEventDetails - Successfully fetched event: " + event.getName());
                            validEvents.add(event);

                            // Update UI immediately when we get at least one event
                            if (validEvents.size() == 1) {
                                Log.d(TAG, "fetchEventDetails - First event received, updating UI");
                                hideEmptyState();
                                eventsAdapter.setEventList(new ArrayList<>(validEvents));
                            }
                        } else {
                            Log.w(TAG, "fetchEventDetails - Event was null for ID: " + eventId);
                        }

                        if (processedCount.incrementAndGet() == eventIds.size()) {
                            Log.d(TAG, "fetchEventDetails - All events processed. Valid events: " + validEvents.size());
                            updateUI(validEvents);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "fetchEventDetails - Failed to fetch event: " + eventId, e);
                        if (processedCount.incrementAndGet() == eventIds.size()) {
                            updateUI(validEvents);
                        }
                    });
        }
    }
    /**
     * Updates the UI with the given list of events.
     *
     * @param events The list of events to display.
     */
    private void updateUI(List<Event> events) {
        if (getActivity() == null || !isAdded()) {
            Log.w(TAG, "updateUI - Fragment not attached, skipping update");
            return;
        }

        Log.d(TAG, "updateUI - Updating with " + events.size() + " events");

        getActivity().runOnUiThread(() -> {
            if (events.isEmpty()) {
                Log.d(TAG, "updateUI - No events, showing empty state");
                showEmptyState();
            } else {
                Log.d(TAG, "updateUI - Has events, showing list");
                hideEmptyState();
                eventsAdapter.setEventList(new ArrayList<>(events));
            }
        });
    }
    /**
     * Shows the empty state for the Home screen.
     */
    private void showEmptyState() {
        if (getActivity() == null || !isAdded()) return;

        getActivity().runOnUiThread(() -> {
            defaultSubtitle.setVisibility(View.VISIBLE);
            getStartedText.setVisibility(View.VISIBLE);
            arrowDown.setVisibility(View.VISIBLE);
            eventsListView.setVisibility(View.GONE);
        });
    }
    /**
     * Hides the empty state for the Home screen.
     */
    private void hideEmptyState() {
        Log.d(TAG, "hideEmptyState called");
        defaultSubtitle.setVisibility(View.GONE);
        getStartedText.setVisibility(View.GONE);
        arrowDown.setVisibility(View.GONE);
        eventsListView.setVisibility(View.VISIBLE);
        Log.d(TAG, "hideEmptyState - RecyclerView visibility: " + eventsListView.getVisibility());

//        if (getActivity() == null || !isAdded()) return;
//
//        getActivity().runOnUiThread(() -> {
//            Log.d(TAG, "hideEmptyState - Setting list visible");
//            defaultSubtitle.setVisibility(View.GONE);
//            getStartedText.setVisibility(View.GONE);
//            arrowDown.setVisibility(View.GONE);
//            eventsListView.setVisibility(View.VISIBLE);
//        });
    }
}
