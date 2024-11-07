package com.bugoff.can_do;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bugoff.can_do.NotificationSettingsActivity;
import com.bugoff.can_do.NotificationsActivity;
import com.bugoff.can_do.event.EventAdapter;
import com.bugoff.can_do.event.EventsListViewModel;

public class HomeActivity extends Fragment {

    private EventsListViewModel eventsListViewModel;
    private RecyclerView recyclerViewHomeEvents;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_screen, container, false);

        // Initialize RecyclerView
        recyclerViewHomeEvents = view.findViewById(R.id.recycler_view_home_events);
        recyclerViewHomeEvents.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize ViewModel
        eventsListViewModel = new ViewModelProvider(this).get(EventsListViewModel.class);

        // Set up adapter for RecyclerView
        final EventAdapter adapter = new EventAdapter();
        recyclerViewHomeEvents.setAdapter(adapter);

        // Observe events data
        eventsListViewModel.getEventsList().observe(getViewLifecycleOwner(), events -> {
            if (events != null && !events.isEmpty()) {
                adapter.setEventList(events);
                view.findViewById(R.id.hs_default_subtitle).setVisibility(View.GONE);
            } else {
                view.findViewById(R.id.hs_default_subtitle).setVisibility(View.VISIBLE);
            }
        });

        //TODO: Add logic to filter and display joined events only
        // Potential approach:
        // 1. Add a method in EventsListViewModel to retrieve only events the user has joined,
        //    such as getJoinedEventsList().
        // 2. Observe this list here in HomeActivity.
        // 3. Update the adapter with joined events using setEventList().

        // Set up existing button functionalities
        view.findViewById(R.id.notif_hs_button).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationsActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.settings_hs_button).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationSettingsActivity.class);
            startActivity(intent);
        });

        return view;
    }
}
