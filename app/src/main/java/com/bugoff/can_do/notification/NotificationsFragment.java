package com.bugoff.can_do.notification;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bugoff.can_do.R;
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.user.User;

import java.util.ArrayList;

/**
 * Fragment for displaying a list of notifications for the current user.
 * Fetches notifications from Firestore using a ViewModel and displays them in a ListView.
 */
public class NotificationsFragment extends Fragment {

    /**
     * Key for passing the user ID as an argument to the fragment.
     */
    private static final String ARG_USER_ID = "user_id";

    /**
     * ListView for displaying notifications.
     */
    private ListView notifListView;

    /**
     * Adapter for populating the ListView with notification data.
     */
    private NotificationAdapter adapter;

    /**
     * ViewModel for managing and observing notification data.
     */
    private NotificationsViewModel notificationsViewModel;

    /**
     * Creates a new instance of NotificationsFragment with the specified user ID.
     *
     * @param userId The ID of the user whose notifications will be displayed.
     * @return A new instance of NotificationsFragment.
     */
    public static NotificationsFragment newInstance(String userId) {
        NotificationsFragment fragment = new NotificationsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called to create the view hierarchy associated with the fragment.
     *
     * @param inflater  The LayoutInflater object to inflate views.
     * @param container The parent container in which the fragment's UI will be displayed.
     * @param savedInstanceState Saved state for restoring the fragment (if applicable).
     * @return The root view of the fragment's layout.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for the fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        // Initialize the ListView and adapter
        notifListView = view.findViewById(R.id.notif_list);
        adapter = new NotificationAdapter(requireContext(), new ArrayList<>());
        notifListView.setAdapter(adapter);

        // Retrieve the user ID from the global repository
        String userId = GlobalRepository.getLoggedInUser().getId();
        if (userId == null) {
            Toast.makeText(getContext(), "User ID is missing", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Initialize the ViewModel and observe notification updates
        notificationsViewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
        notificationsViewModel.getNotifications(userId).observe(getViewLifecycleOwner(), notifications -> {
            adapter.clear();
            adapter.addAll(notifications);
            adapter.notifyDataSetChanged();
        });

        // Set up the back button to return to the previous screen
        ImageButton backButton = view.findViewById(R.id.notif_back_button);
        backButton.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        return view; // Return the created view
    }
}
