package com.bugoff.can_do;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.event.EventAdapter;
import com.bugoff.can_do.notification.NotificationSettingsActivity;
import com.bugoff.can_do.notification.NotificationsFragment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

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

        eventsAdapter = new EventAdapter(new ArrayList<>(), false, null);
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

    private void fetchEventDetails(List<String> eventIds) {
        List<Event> events = new ArrayList<>();

        for (String eventId : eventIds) {
            GlobalRepository.getEvent(eventId).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Event event = task.getResult();
                    if (event != null) {
                        events.add(event);
                    }
                    // Check if all events are loaded
                    if (events.size() == eventIds.size()) {
                        eventsAdapter.setEventList(events);
                    }
                } else {
                    // Handle the error
                    Log.e("RepositoryError", "Error getting event: " + task.getException());
                }
            });
        }
    }

//    @Override
//    public void onItemClick(Event event) {
//        // Handle the click event here, e.g., navigate to event details
//        Toast.makeText(getContext(), "Clicked on: " + event.getName(), Toast.LENGTH_SHORT).show();
//        // You can also start a new fragment or activity with event details
//
//    }
}




