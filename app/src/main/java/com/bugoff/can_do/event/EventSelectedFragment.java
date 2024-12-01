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
import java.util.Map;

/**
 * Fragment to display details of a selected event, including a list of users associated with the event.
 * Shows a RecyclerView of users, and manages visibility of a progress bar and empty state message.
 */
public class EventSelectedFragment extends Fragment {

    private static final String TAG = "EventSelectedFragment";

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private ProgressBar progressBar;
    private TextView emptyTextView;

    private List<User> userList = new ArrayList<>();

    private String eventId; // ID of the selected event, used to fetch relevant data

    private EventViewModel viewModel; // ViewModel to manage event data

    private Map<String, User> selectedUsersMap;
    private Map<String, User> enrolledUsersMap;

    /**
     * Default constructor required for fragment instantiation.
     */
    public EventSelectedFragment() {
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
            Log.e(TAG, "No eventId provided to EventSelectedFragment.");
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
        return inflater.inflate(R.layout.fragment_event_selected, container, false);
    }

    /**
     * Sets up the RecyclerView, initializes the ViewModel, and observes LiveData when the view is created.
     *
     * @param view               The root view of the fragment's layout.
     * @param savedInstanceState Bundle containing saved instance state data.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycler_view_selected_users);
        progressBar = view.findViewById(R.id.progress_bar_selected);
        emptyTextView = view.findViewById(R.id.text_view_empty_selected);

        // Initialize RecyclerView with a LinearLayoutManager and UserAdapter
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userAdapter = new UserAdapter(userList, null);
        recyclerView.setAdapter(userAdapter);

        // Initialize ViewModel using a factory with the event ID
        EventViewModelFactory factory = new EventViewModelFactory(eventId);
        viewModel = new ViewModelProvider(this, factory).get(EventViewModel.class);

        // Observe LiveData for selected list users
        viewModel.getSelectedEntrantsUsers().observe(getViewLifecycleOwner(), selectedUsersMap -> {
            Log.d(TAG, "Observer: Received usersMap with size: " + (selectedUsersMap != null ? selectedUsersMap.size() : "null"));
            if (selectedUsersMap != null) {
                this.selectedUsersMap = selectedUsersMap;
                updateUserList();
            }
        });

        // Observe LiveData for enrolled list users
        viewModel.getEnrolledEntrantsUsers().observe(getViewLifecycleOwner(), enrolledUsersMap -> {
            Log.d(TAG, "Observer: Received usersMap with size: " + (enrolledUsersMap != null ? enrolledUsersMap.size() : "null"));
            if (enrolledUsersMap != null) {
                this.enrolledUsersMap = enrolledUsersMap;
                updateUserList();
            }
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

    private void updateUserList() {
        userList.clear();

        if (selectedUsersMap != null && !selectedUsersMap.isEmpty()) {
            userList.addAll(selectedUsersMap.values());
        }

        if (enrolledUsersMap != null && !enrolledUsersMap.isEmpty()) {
            for (User enrolledUser : enrolledUsersMap.values()) {
                if (!userList.contains(enrolledUser)) {
                    userList.add(enrolledUser);
                }
            }
        }

        userAdapter.notifyDataSetChanged();

        if (userList.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText("No users in the selected list.");
        } else {
            emptyTextView.setVisibility(View.GONE);
        }

        progressBar.setVisibility(View.GONE);
    }
}
