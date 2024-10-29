package com.bugoff.can_do;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeActivity extends Fragment {

    ImageButton notifButton;
    ImageButton settingsButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_screen, container, false);

        notifButton = view.findViewById(R.id.notif_hs_button);
        settingsButton = view.findViewById(R.id.settings_hs_button);

        // Functionality of notifications button on HomeScreen
        notifButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Change to Notification Icon!", Toast.LENGTH_SHORT).show();
            }
        });

        // Functionality of settings button on HomeScreen
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Change to Settings Icon!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
