package com.bugoff.can_do;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bugoff.can_do.organizer.OrganizerTransition;

public class UserProfileActivity extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_profile_screen, container, false);

        // If user clicks "im an organizer"
        view.findViewById(R.id.organizer_button).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OrganizerTransition.class);
            startActivity(intent);
        });

        // If user clicks "Notification Settings"
        view.findViewById(R.id.notif_button).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationSettingsActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.name_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editNameDialog();
            }
        });

        view.findViewById(R.id.email_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editEmailDialog();
            }
        });

        view.findViewById(R.id.pnumber_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (phone number is null) {
//                    addPNumberDialog();
//                } else {
//                    editPNumberDialog();
//                }
            }
        });

        return view;
    }

    private void editNameDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View nameView = inflater.inflate(R.layout.fragment_edit_name, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder
                .setView(nameView)
                .setNeutralButton("CANCEL", null)
                .setPositiveButton("CONFIRM", (dialog, which) -> {
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    private void editEmailDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View emailView = inflater.inflate(R.layout.fragment_edit_email, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder
                .setView(emailView)
                .setNeutralButton("CANCEL", null)
                .setPositiveButton("CONFIRM", (dialog, which) -> {
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    private void addPNumberDialog() {
        // add phone number function here
    }

    private void editPNumberDialog() {
        // edit phone number function here
    }
}
