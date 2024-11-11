package com.bugoff.can_do;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.notification.NotificationSettingsActivity;
import com.bugoff.can_do.notification.NotificationsFragment;

/**
 * Fragment for the Home screen.
 */
public class HomeActivity extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_screen, container, false);


        String userId = GlobalRepository.getLoggedInUser().getId();

        view.findViewById(R.id.notif_hs_button).setOnClickListener(v -> {
            NotificationsFragment notificationsFragment = NotificationsFragment.newInstance(userId);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, notificationsFragment)
                    .addToBackStack(null)
                    .commit();
        });



        // Functionality of settings button on HomeScreen
        view.findViewById(R.id.settings_hs_button).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationSettingsActivity.class);
            startActivity(intent);
        });

        return view;
    }
}
