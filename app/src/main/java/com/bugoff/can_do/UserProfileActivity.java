package com.bugoff.can_do;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bugoff.can_do.organizer.FacilityEdit;

public class UserProfileActivity extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_profile_screen, container, false);

        // If user clicks "im an organizer"
        view.findViewById(R.id.button10).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FacilityEdit.class);
            startActivity(intent);
        });

        return view;
    }
}