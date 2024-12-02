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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying a list of notifications for the current user.
 * This fragment fetches the user's notification list from Firestore and displays it in a ListView.
 */
public class NotificationsFragment extends Fragment {

    private static final String ARG_USER_ID = "user_id";
    private ListView notifListView;
    private NotificationAdapter adapter;
    private NotificationsViewModel notificationsViewModel;

    public static NotificationsFragment newInstance(String userId) {
        NotificationsFragment fragment = new NotificationsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        notifListView = view.findViewById(R.id.notif_list);
        adapter = new NotificationAdapter(requireContext(), new ArrayList<>());
        notifListView.setAdapter(adapter);

        // Retrieve user ID from arguments
        String userId = GlobalRepository.getLoggedInUser().getId();
        if (userId == null) {
            Toast.makeText(getContext(), "User ID is missing", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Initialize ViewModel and observe notifications
        notificationsViewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
        notificationsViewModel.getNotifications(userId).observe(getViewLifecycleOwner(), notifications -> {
            adapter.clear();
            adapter.addAll(notifications);
            adapter.notifyDataSetChanged();
        });

        // Set up back button
        ImageButton backButton = view.findViewById(R.id.notif_back_button);
        backButton.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        return view;
    }

}
