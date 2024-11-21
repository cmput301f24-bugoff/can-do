package com.bugoff.can_do.event;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bugoff.can_do.MainActivity;
import com.bugoff.can_do.R;
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.organizer.CreateEventFragment;
import com.bugoff.can_do.organizer.EventDetailsFragmentOrganizer;
import com.bugoff.can_do.organizer.OrganizerMain;
import com.bugoff.can_do.user.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

/**
 * Fragment to display a list of events.
 * Shows a RecyclerView of events, and manages visibility of a FAB to add events.
 */
public class EventsFragment extends Fragment {
    private static final String ARG_IS_ADMIN = "isAdmin";
    private boolean isAdmin;

    private RecyclerView recyclerViewEvents;
    private EventAdapter eventAdapter;
    private FloatingActionButton fabAddEvent;
    private EventsListViewModel eventsListViewModel;
    private EventsListViewModel viewModel;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param isAdmin Boolean flag indicating if the user is an admin.
     * @return A new instance of fragment EventsFragment.
     */
    public static EventsFragment newInstance(boolean isAdmin) {
        EventsFragment fragment = new EventsFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_ADMIN, isAdmin);
        fragment.setArguments(args);
        return fragment;
    }

    public EventsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isAdmin = false;
        User currentUser = GlobalRepository.getLoggedInUser();
        if (currentUser != null) {
            isAdmin = currentUser.getIsAdmin() != null ? currentUser.getIsAdmin() : false;
        }

        // Get the repository from the activity if we're testing
        GlobalRepository repository = null;
        if (getActivity() instanceof MainActivity) {
            repository = ((MainActivity) getActivity()).getRepository();
        } else if (getActivity() instanceof OrganizerMain) {
            repository = ((OrganizerMain) getActivity()).getRepository();
        }

        // Create the ViewModel using the appropriate factory
        EventsListViewModelFactory factory;
        if (repository != null) {
            factory = new EventsListViewModelFactory(repository, isAdmin);
        } else {
            factory = new EventsListViewModelFactory(isAdmin);
        }

        eventsListViewModel = new ViewModelProvider(this, factory).get(EventsListViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_events, container, false);

        recyclerViewEvents = view.findViewById(R.id.recycler_view_events);
        fabAddEvent = view.findViewById(R.id.fab_add_event);

        View fragmentContainer = getActivity().findViewById(R.id.fragment_container);
        if (fragmentContainer == null) {
            Log.e("EventsFragment", "fragment_container not found in activity layout.");
        }

        // Set up RecyclerView
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        eventAdapter = new EventAdapter(new ArrayList<>(), isAdmin, this::onDeleteEventClick);
        recyclerViewEvents.setAdapter(eventAdapter);

        // Initialize ViewModel with isAdmin flag
        EventsListViewModelFactory factory = new EventsListViewModelFactory(isAdmin);
        eventsListViewModel = new ViewModelProvider(this, factory).get(EventsListViewModel.class);

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

        // Observe LiveData for status messages
        eventsListViewModel.getStatusMessage().observe(getViewLifecycleOwner(), statusMsg -> {
            if (statusMsg != null && !statusMsg.isEmpty()) {
                Toast.makeText(getContext(), statusMsg, Toast.LENGTH_SHORT).show();
            }
        });

        // Set up FAB listener to open CreateEventFragment
        fabAddEvent.setVisibility(View.VISIBLE);
        fabAddEvent.setOnClickListener(v -> {
            Fragment createEventFragment = CreateEventFragment.newInstance();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, createEventFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }

    /**
     * Handles the delete event action triggered from the adapter.
     *
     * @param event The event to be deleted.
     */
    private void onDeleteEventClick(Event event) {
        // Show a confirmation dialog
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete the event \"" + event.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Call ViewModel to delete the event
                    eventsListViewModel.deleteEvent(event);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Handles clicks on the entire event item to view details.
     *
     * @param event The event clicked.
     */
    private void onEventClicked(Event event) {
        EventDetailsFragmentOrganizer eventDetailsFragment = EventDetailsFragmentOrganizer.newInstance(event.getId());

        // Assuming getContext() returns an activity that has a FragmentManager
        AppCompatActivity activity = (AppCompatActivity) getContext();
        if (activity != null) {
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, eventDetailsFragment) // Replace with your fragment container ID
                    .addToBackStack(null) // Optionally add to back stack
                    .commit();
        }
    }

}
