package com.bugoff.can_do.event;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bugoff.can_do.R;
import com.bugoff.can_do.user.User;
import com.bugoff.can_do.user.UserAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EventWaitlistFragment extends Fragment {

    private static final String TAG = "EventWaitlistFragment";

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private ProgressBar progressBar;
    private TextView emptyTextView;

    private List<User> userList = new ArrayList<>();

    private String eventId;

    private EventViewModel viewModel;

    public EventWaitlistFragment() {
        // Required empty public constructor
    }

    // If passing arguments
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventId");
            Log.d(TAG, "Event ID: " + eventId);
        } else {
            Log.e(TAG, "No eventId provided to EventWaitlistFragment.");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_waitlist, container, false);
    }

    private void performDrawing(int numberToDraw) {
        // Randomly select users from the waitlist
        List<User> selectedUsers = new ArrayList<>();
        List<User> waitlistCopy = new ArrayList<>(userList); // Create a copy to avoid modifying the original list

        Random random = new Random();
        for (int i = 0; i < numberToDraw; i++) {
            int index = random.nextInt(waitlistCopy.size());
            selectedUsers.add(waitlistCopy.get(index));
            waitlistCopy.remove(index);
        }

        for (User user : selectedUsers) {
            // Update the selected list in the ViewModel and database
            viewModel.addSelectedEntrant(user.getId());

            // Remove the selected users from the waitlist
            viewModel.removeWaitingListEntrant(user.getId());
        }

        // Update the UI
        userList.removeAll(selectedUsers);
        userAdapter.notifyDataSetChanged();

        Toast.makeText(getContext(), "Successfully drew " + numberToDraw + " users.", Toast.LENGTH_SHORT).show();
    };
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycler_view_waitlist_users);
        progressBar = view.findViewById(R.id.progress_bar_waitlist);
        emptyTextView = view.findViewById(R.id.text_view_empty_waitlist);

        // Initialize RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userAdapter = new UserAdapter(userList);
        recyclerView.setAdapter(userAdapter);

        // Initialize ViewModel
        EventViewModelFactory factory = new EventViewModelFactory(eventId);
        viewModel = new ViewModelProvider(this, factory).get(EventViewModel.class);

        // Observe LiveData for waiting list users
        viewModel.getWaitingListUsers().observe(getViewLifecycleOwner(), usersMap -> {
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
                emptyTextView.setText("No users in the watch list.");
                Log.d(TAG, "Observer: userList is empty, showing emptyTextView");
            }
            progressBar.setVisibility(View.GONE);
        });

        // Get reference to the "Draw" button
        Button drawButton = view.findViewById(R.id.draw);
        drawButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Enter number of users to draw");

            final EditText input = new EditText(getContext());
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            builder.setView(input);

            builder.setPositiveButton("Draw", (dialog, which) -> {
                String inputText = input.getText().toString().trim();
                if (!inputText.isEmpty()) {
                    try {
                        int numberToDraw = Integer.parseInt(inputText);
                        if (numberToDraw <= 0) {
                            Toast.makeText(getContext(), "Please enter a positive number.", Toast.LENGTH_SHORT).show();
                        } else if (numberToDraw > userList.size()) {
                            Toast.makeText(getContext(), "Number exceeds the waitlist size.", Toast.LENGTH_SHORT).show();
                        } else {
                            performDrawing(numberToDraw);
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Invalid number.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Please enter a number.", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                progressBar.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.VISIBLE);
                emptyTextView.setText(error);
            }
        });
    }
}
