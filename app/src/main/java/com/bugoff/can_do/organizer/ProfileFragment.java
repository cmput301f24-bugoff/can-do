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

/**
 * A Fragment representing the user's profile in the organizer context. This fragment allows the user
 * to either navigate to the home screen as an attendee or manage their facility.
 */
public class ProfileFragment extends Fragment {

    /**
     * Called to inflate the fragment's view. It sets up click listeners for the buttons that allow the user
     * to navigate to different activities: either the home screen as an attendee or the facility management screen.
     *
     * @param inflater The LayoutInflater object to inflate views in the fragment.
     * @param container The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this Bundle contains the fragment's previously saved state.
     * @return The inflated view for this fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_profile_organizer, container, false);

        // If user clicks "im an attendee"
        view.findViewById(R.id.attendee_button).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        });

        // If user clicks "Manage Facility"
        view.findViewById(R.id.manage_facility_button).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FacilityEdit.class);
            startActivity(intent);
        });
        return view;
    }
}
