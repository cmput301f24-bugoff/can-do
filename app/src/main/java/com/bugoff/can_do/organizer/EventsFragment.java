package com.bugoff.can_do.organizer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bugoff.can_do.R;
import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.event.EventAdapter;
import com.bugoff.can_do.event.EventsListViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class EventsFragment extends Fragment {
    private static final String TAG = "EventsFragment";

    private RecyclerView recyclerViewEvents;
    private EventAdapter eventAdapter;
    private FloatingActionButton fabAddEvent;
    private EventsListViewModel eventsListViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_events, container, false);

        recyclerViewEvents = view.findViewById(R.id.recycler_view_events);
        fabAddEvent = view.findViewById(R.id.fab_add_event);

        // Set up RecyclerView
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        eventAdapter = new EventAdapter(new ArrayList<>(), this::onEventClicked);
        recyclerViewEvents.setAdapter(eventAdapter);

        // Initialize ViewModel
        eventsListViewModel = new ViewModelProvider(this).get(EventsListViewModel.class);

        // Observe LiveData for events list
        eventsListViewModel.getEventsList().observe(getViewLifecycleOwner(), events -> {
            if (events != null) {
                eventAdapter.setEventList(events);
            }
        });

        // Observe LiveData for error messages
        eventsListViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });

        // Set up FAB listener to open CreateEventFragment
        fabAddEvent.setOnClickListener(v -> {
            Fragment createEventFragment = CreateEventFragment.newInstance();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container_organizer, createEventFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }

    private void onEventClicked(Event event) {
        // TODO: Implement event item click handling
    }
}
