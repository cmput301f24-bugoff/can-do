package com.bugoff.can_do.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bugoff.can_do.HomeActivity;
import com.bugoff.can_do.MainActivity;
import com.bugoff.can_do.R;
import com.bugoff.can_do.organizer.OrganizerTransition;

public class ProfileFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_profile_organizer, container, false);

        // If user clicks "im an attendee"
        view.findViewById(R.id.attendee_button).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        });

        return view;
    }
}
