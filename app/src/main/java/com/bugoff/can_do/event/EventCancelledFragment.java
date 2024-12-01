package com.bugoff.can_do.event;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bugoff.can_do.R;
import com.bugoff.can_do.user.User;
import com.bugoff.can_do.user.UserAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying and managing the list of cancelled users for a specific event.
 * Provides functionality to view the list of cancelled users.
 */
public class EventCancelledFragment extends Fragment {

    private static final String TAG = "EventCancelledFragment";

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private ProgressBar progressBar;
    private TextView emptyTextView;

    private List<User> userList = new ArrayList<>(); // List of users in the cancelled list
    private String eventId; // ID of the associated event
    private EventViewModel viewModel; // ViewModel to manage event-related data

    /**
     * Default constructor required for fragment instantiation.
     */
    public EventCancelledFragment() {
        // Required empty public constructor
    }

    /**
     * Initializes the fragment, extracting the event ID from arguments if available.
     *
     * @param savedInstanceState Bundle containing saved instance state data.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventId");
            Log.d(TAG, "Event ID: " + eventId);
        } else {
            Log.e(TAG, "No eventId provided to EventCancelledFragment.");
        }
    }

    /**
     * Inflates the layout for this fragment.
     *
     * @param inflater           LayoutInflater used to inflate views in the fragment.
     * @param container          Parent view that the fragment's UI will attach to.
     * @param savedInstanceState Bundle containing saved instance state data.
     * @return The root view of the inflated layout.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_cancelled, container, false);
    }

    /**
     * Initializes views, sets up the RecyclerView and ViewModel, and observes data changes for the cancelled users list.
     *
     * @param view               The root view of the fragment's layout.
     * @param savedInstanceState Bundle containing saved instance state data.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycler_view_cancelled_users);
        progressBar = view.findViewById(R.id.progress_bar_cancelled);
        emptyTextView = view.findViewById(R.id.text_view_empty_cancelled);

        // Initialize RecyclerView with a LinearLayoutManager and UserAdapter
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userAdapter = new UserAdapter(userList, null);
        recyclerView.setAdapter(userAdapter);

        // Initialize ViewModel using a factory with the event ID
        EventViewModelFactory factory = new EventViewModelFactory(eventId);
        viewModel = new ViewModelProvider(this, factory).get(EventViewModel.class);

        // Observe LiveData for cancelled users list
        viewModel.getCancelledUsers().observe(getViewLifecycleOwner(), usersMap -> {
            Log.d(TAG, "Observer: Received usersMap with size: " + (usersMap != null ? usersMap.size() : "null"));
            if (usersMap != null && !usersMap.isEmpty()) {
                userList.clear();
                userList.addAll(usersMap.values());
                userAdapter.notifyDataSetChanged();
                emptyTextView.setVisibility(View.GONE);
                Log.d(TAG, "Observer: Updated userList and notified adapter");
            } else {
                userList.clear();
                userAdapter.notifyDataSetChanged();
                emptyTextView.setVisibility(View.VISIBLE);
                emptyTextView.setText("No users in the cancelled list.");
                Log.d(TAG, "Observer: userList is empty, showing emptyTextView");
            }
            progressBar.setVisibility(View.GONE);
        });

        // Observe error messages from ViewModel
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                progressBar.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.VISIBLE);
                emptyTextView.setText(error);
            }
        });
    }
}
