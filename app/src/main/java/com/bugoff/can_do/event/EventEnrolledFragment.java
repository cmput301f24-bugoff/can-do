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

public class EventEnrolledFragment extends Fragment {
    private static final String TAG = "EventEnrolledFragment";
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private List<User> userList = new ArrayList<>();
    private String eventId;
    private EventViewModel viewModel;

    public EventEnrolledFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventId");
            Log.d(TAG, "Event ID: " + eventId);
        } else {
            Log.e(TAG, "No eventId provided to EventEnrolledFragment.");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_enrolled, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycler_view_enrolled_users);
        progressBar = view.findViewById(R.id.progress_bar_enrolled);
        emptyTextView = view.findViewById(R.id.text_view_empty_enrolled);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userAdapter = new UserAdapter(userList, null);
        recyclerView.setAdapter(userAdapter);

        EventViewModelFactory factory = new EventViewModelFactory(eventId);
        viewModel = new ViewModelProvider(this, factory).get(EventViewModel.class);

        viewModel.getEnrolledEntrantsUsers().observe(getViewLifecycleOwner(), usersMap -> {
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
                emptyTextView.setText("No enrolled users found.");
                Log.d(TAG, "Observer: userList is empty, showing emptyTextView");
            }
            progressBar.setVisibility(View.GONE);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                progressBar.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.VISIBLE);
                emptyTextView.setText(error);
            }
        });
    }
}