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

        // If user already added a phone number, change visibility to "Edit Phone Number" button
            //-- if condition here --//
        // view.findViewById(R.id.input_add_pnumber).setVisibility(View.INVISIBLE);
        // view.findViewById(R.id.edit_pnumber_button).setVisibility(View.VISIBLE);

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

        view.findViewById(R.id.add_pnumber_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //-- create if condition here --//

                // If no phone number added yet
                addPNumberDialog();

                // Else if there is already a phone number; to edit
                // editPNumberDialog()
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
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View pnumberView = inflater.inflate(R.layout.fragment_pnumber, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder
                .setView(pnumberView)
                .setNeutralButton("CANCEL", null)
                .setPositiveButton("CONFIRM", (dialog, which) -> {
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    private void editPNumberDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View pnumberView = inflater.inflate(R.layout.fragment_pnumber, null);

        pnumberView.findViewById(R.id.input_add_pnumber).setVisibility(View.INVISIBLE);
        pnumberView.findViewById(R.id.input_edit_pnumber).setVisibility(View.VISIBLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder
                .setView(pnumberView)
                .setNeutralButton("CANCEL", null)
                .setPositiveButton("CONFIRM", (dialog, which) -> {
                    dialog.dismiss();
                })
                .create()
                .show();
    }
}
