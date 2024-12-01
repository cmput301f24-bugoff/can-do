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
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.notification.Notification;
import com.bugoff.can_do.user.User;
import com.bugoff.can_do.user.UserAdapter;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Fragment for displaying and managing the waitlist of users for a specific event.
 * Provides functionality to randomly select users from the waitlist and move them to the selected list.
 * Displays a RecyclerView of waitlisted users, a progress bar during data load, and handles error messages.
 */
public class EventWaitlistFragment extends Fragment {

    private static final String TAG = "EventWaitlistFragment";

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private ProgressBar progressBar;
    private TextView emptyTextView;

    private List<User> userList = new ArrayList<>(); // List of users on the waitlist
    private String eventId; // ID of the associated event
    private EventViewModel viewModel; // ViewModel to manage event-related data

    /**
     * Default constructor required for fragment instantiation.
     */
    public EventWaitlistFragment() {
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
            Log.e(TAG, "No eventId provided to EventWaitlistFragment.");
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
        return inflater.inflate(R.layout.fragment_event_waitlist, container, false);
    }

    /**
     * Randomly selects a specified number of users from the waitlist and adds them to the selected entrants list.
     * Updates the ViewModel and removes selected users from the waitlist in the UI.
     *
     * @param numberToDraw The number of users to randomly select from the waitlist.
     */
    private void performDrawing(int numberToDraw) {
        // Randomly select users from the waitlist
        Random random = new Random();
        List<User> selectedUsers = new ArrayList<>();
        for (int i = 0; i < numberToDraw && !userList.isEmpty(); i++) {
            int randomIndex = random.nextInt(userList.size());
            User selectedUser = userList.get(randomIndex);
            selectedUsers.add(selectedUser);
            userList.remove(randomIndex);
            Log.d(TAG, "performDrawing: " + selectedUser.getId() + " selected");
            userAdapter.notifyItemRemoved(randomIndex);
        }
        if (userList.isEmpty()) {
            // Handle empty state if necessary
            Log.d(TAG, "Observer: userList is empty");
        }

        // Update Firestore document
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("events").document(eventId);

        // Fetch current lists and update
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> waitingListEntrants = (List<String>) documentSnapshot.get("waitingListEntrants");
                List<String> selectedEntrants = (List<String>) documentSnapshot.get("selectedEntrants");

                // Prepare to move users from waitingList to selected
                if (waitingListEntrants != null && selectedEntrants != null) {
                    List<String> selectedUserIds = new ArrayList<>();

                    // Gather selected user IDs and remove from waiting list
                    for (User selectedUser : selectedUsers) {
                        String userId = selectedUser.getId();
                        selectedUserIds.add(userId);
                        waitingListEntrants.remove(userId);
                    }
                    // Add selected users to selectedEntrants list
                    selectedEntrants.addAll(selectedUserIds);

                    // Update Firestore document with new lists
                    docRef.update("waitingListEntrants", waitingListEntrants, "selectedEntrants", selectedEntrants)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Drawing completed and lists updated");

                                // Create and send notification to newly selected users
                                if (!selectedUserIds.isEmpty()) {
                                    String uniqueId = UUID.randomUUID().toString();
                                    Notification notification = new Notification(
                                            uniqueId,
                                            "Selection Update",
                                            "You have been selected to participate!",
                                            documentSnapshot.getString("facilityId"),
                                            new ArrayList<>(selectedUserIds),
                                            eventId
                                    );
                                    GlobalRepository.addNotification(notification);
                                }
                            })
                            .addOnFailureListener(e -> Log.e(TAG, "Error updating Firestore lists", e));
                }
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Failed to retrieve document", e));

        Toast.makeText(getContext(), "Successfully selected " + numberToDraw + " users.", Toast.LENGTH_SHORT).show();
    }


    /**
     * Initializes views, sets up the RecyclerView and ViewModel, and observes data changes for the waitlist users.
     * Handles the "Draw" button click to trigger user selection from the waitlist.
     *
     * @param view               The root view of the fragment's layout.
     * @param savedInstanceState Bundle containing saved instance state data.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycler_view_waitlist_users);
        progressBar = view.findViewById(R.id.progress_bar_waitlist);
        emptyTextView = view.findViewById(R.id.text_view_empty_waitlist);

        // Initialize RecyclerView with a LinearLayoutManager and UserAdapter
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userAdapter = new UserAdapter(userList, null);
        recyclerView.setAdapter(userAdapter);

        // Initialize ViewModel using a factory with the event ID
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

        // Set up "Draw" button click listener to select users from the waitlist
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

