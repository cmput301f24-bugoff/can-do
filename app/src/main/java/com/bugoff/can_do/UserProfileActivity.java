package com.bugoff.can_do;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bugoff.can_do.admin.AdminActivity;
import com.bugoff.can_do.notification.NotificationSettingsActivity;
import com.bugoff.can_do.organizer.OrganizerTransition;
import com.bugoff.can_do.user.User;
import com.bugoff.can_do.user.UserViewModel;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Fragment for the user profile screen.
 */
public class UserProfileActivity extends Fragment {
    private User user;
    private UserViewModel userViewModel;
    private String currEmail;
    private String currPNumber;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the UserViewModel from the MainActivity
        MainActivity activity = (MainActivity) requireActivity();
        userViewModel = activity.getUserViewModel();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_profile_screen, container, false);

        // Set up UserViewModel observers
        userViewModel.getUserName().observe(getViewLifecycleOwner(), name -> {
            if (name != null) {
                TextView firstName = view.findViewById(R.id.first_name);
                TextView lastName = view.findViewById(R.id.last_name);
                String[] parts = name.split(" ");
                if (parts.length > 0) {
                    firstName.setText(parts[0]);
                    // Update avatar with first letter of name
                    String firstLetter = parts[0].isEmpty() ? "A" : parts[0].substring(0, 1).toUpperCase();
                    ImageView avatar = view.findViewById(R.id.image_avatar);
                    avatar.setImageBitmap(generateAvatar(firstLetter));
                }
                if (parts.length > 1) {
                    lastName.setText(parts[1]);
                }
            }
        });

        userViewModel.getEmail().observe(getViewLifecycleOwner(), email -> {
            currEmail = email;
        });

        userViewModel.getPhoneNumber().observe(getViewLifecycleOwner(), phoneNumber -> {
            currPNumber = phoneNumber;
            updatePhoneNumberButtonVisibility(view, phoneNumber);
        });

        userViewModel.getIsAdmin().observe(getViewLifecycleOwner(), isAdmin -> {
            setupAdminButton(view, isAdmin);
        });

        // Set up click listeners
        view.findViewById(R.id.name_button).setOnClickListener(v -> {
            TextView firstName = view.findViewById(R.id.first_name);
            TextView lastName = view.findViewById(R.id.last_name);
            editNameDialog(firstName, lastName);
        });

        view.findViewById(R.id.email_button).setOnClickListener(v -> {
            editEmailDialog();
        });

        view.findViewById(R.id.add_pnumber_button).setOnClickListener(v -> {
            addPNumberDialog();
        });

        view.findViewById(R.id.edit_pnumber_button).setOnClickListener(v -> {
            editPNumberDialog();
        });

        view.findViewById(R.id.notif_button).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationSettingsActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.organizer_button).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OrganizerTransition.class);
            startActivity(intent);
        });

        // Set up avatar click listener
        view.findViewById(R.id.image_avatar).setOnClickListener(v -> {
            TextView firstName = view.findViewById(R.id.first_name);
            String userName = firstName.getText().toString();
            String firstLetter = userName.isEmpty() ? "A" : userName.substring(0, 1).toUpperCase();
            ImageView avatar = view.findViewById(R.id.image_avatar);

            new AlertDialog.Builder(getContext())
                    .setTitle("Change Profile Picture")
                    .setMessage("Choose a source")
                    .setPositiveButton("Gallery", (dialog, which) -> {
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        galleryLauncher.launch(galleryIntent);
                    })
                    .setNegativeButton("Camera", (dialog, which) -> {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        cameraLauncher.launch(cameraIntent);
                    })
                    .setNeutralButton("Remove", (dialog, which) -> {
                        avatar.setImageBitmap(generateAvatar(firstLetter));
                    })
                    .show();
        });

        // Initialize admin button state (will be updated by observer)
        Button adminButton = view.findViewById(R.id.admin_button);
        adminButton.setVisibility(View.GONE);

        // Initialize phone number button states (will be updated by observer)
        view.findViewById(R.id.add_pnumber_button).setVisibility(View.VISIBLE);
        view.findViewById(R.id.edit_pnumber_button).setVisibility(View.GONE);

        return view;
    }

    /**
     * Helper for generating default avatar when custom avatar is removed
     *
     * @param letter First letter of user's name
     * @return Bitmap of default avatar
     */
    // Helper for generating default avatar when custom avatar is removed
    private Bitmap generateAvatar(String letter) {
        int size = 175;
        int bgColor = Color.LTGRAY;
        int txtColor = Color.WHITE;

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint bgPaint = new Paint();
        bgPaint.setColor(bgColor);
        bgPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, size, size, bgPaint);

        Paint txtPaint = new Paint();
        txtPaint.setColor(txtColor);
        txtPaint.setTextSize((float) size / 2);
        txtPaint.setTextAlign(Paint.Align.CENTER);
        txtPaint.setAntiAlias(true);

        float x = (float) size / 2;
        float y = (float) (size / 2) - ((txtPaint.descent() + txtPaint.ascent()) / 2);
        canvas.drawText(letter, x, y, txtPaint);

        return bitmap;
    }

    /**
     * Helper for launching gallery and camera intents
     */
    // For gallery launching
    private ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    ImageView avatar = getView().findViewById(R.id.image_avatar);
                    avatar.setImageURI(selectedImageUri);
                }
            }
    );

    /**
     * Helper for launching gallery and camera intents
     */
    // For camera launching
    private ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("Data");
                    ImageView avatar = getView().findViewById(R.id.image_avatar);
                    avatar.setImageBitmap(photo);
                }
            }
    );

    /**
     * Helper for loading user data from Firestore
     *
     * @param db        Firestore instance
     * @param androidID Android ID of the user
     */
    private void loadUserData(FirebaseFirestore db, String androidID) {
        db.collection("users").document(androidID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        user = new User(documentSnapshot);

                        String userName = user.getName();
                        TextView firstName = getView().findViewById(R.id.first_name);
                        TextView lastName = getView().findViewById(R.id.last_name);

                        if (userName != null && !userName.isEmpty()) {
                            String[] parts = userName.split(" ");
                            if (parts.length > 0) {
                                firstName.setText(parts[0]);
                            }
                            if (parts.length > 1) {
                                lastName.setText(parts[1]);
                            }

                            // Automatically set profile picture to default avatar
                            String avatarFirstName = parts[0];
                            String firstLetter = avatarFirstName.isEmpty() ? "A" : avatarFirstName.substring(0, 1).toUpperCase();
                            ImageView avatar = getView().findViewById(R.id.image_avatar);
                            avatar.setImageBitmap(generateAvatar(firstLetter));
                        }

                        String oldEmail = user.getEmail();
                        if (oldEmail != null && !oldEmail.isEmpty()) {
                            currEmail = oldEmail;
                        }

                        String phNumber = user.getPhoneNumber();
                        if (phNumber != null && !phNumber.isEmpty()) {
                            currPNumber = phNumber;

                            // If user already added a phone number, change visibility to "Edit Phone Number" button
                            getView().findViewById(R.id.add_pnumber_button).setVisibility(View.INVISIBLE);
                            getView().findViewById(R.id.edit_pnumber_button).setVisibility(View.VISIBLE);
                        }

                    } else {
                        user = new User(androidID);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failure to load data", Toast.LENGTH_SHORT).show());
    }

    /**
     * Helper for saving user data to Firestore
     *
     * @param firstNameTxt First name TextView
     * @param lastNameTxt  Last name TextView
     */
    private void saveUserData(TextView firstNameTxt, TextView lastNameTxt) {
        String name = firstNameTxt.getText().toString() + " " + lastNameTxt.getText().toString();
        userViewModel.setName(name);
    }

    private void saveUserEmail(String email) {
        userViewModel.setEmail(email);
    }

    private void saveUserPNumber(String pnumber) {
        userViewModel.setPhoneNumber(pnumber);
    }

    /**
     * Helper for editing user name
     *
     * @param firstName First name TextView
     * @param lastName  Last name TextView
     */
    private void editNameDialog(TextView firstName, TextView lastName) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View nameView = inflater.inflate(R.layout.fragment_edit_name, null);

        String first = firstName.getText().toString();
        String last = lastName.getText().toString();

        EditText editFirstName = nameView.findViewById(R.id.input_first_name);
        EditText editLastName = nameView.findViewById(R.id.input_last_name);

        editFirstName.setText(first);
        editLastName.setText(last);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder
                .setView(nameView)
                .setNeutralButton("CANCEL", null)
                .setPositiveButton("CONFIRM", (dialog, which) -> {
                    String newFirstName = editFirstName.getText().toString();
                    String newLastName = editLastName.getText().toString();

                    firstName.setText(newFirstName);
                    lastName.setText(newLastName);

                    saveUserData(firstName, lastName);
                })
                .create()
                .show();
    }

    /**
     * Helper for editing user email
     */
    private void editEmailDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View emailView = inflater.inflate(R.layout.fragment_edit_email, null);

        TextView oldEmail = emailView.findViewById(R.id.textview_old_email);
        EditText editEmail = emailView.findViewById(R.id.input_new_email);

        oldEmail.setText(currEmail);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder
                .setView(emailView)
                .setNeutralButton("CANCEL", null)
                .setPositiveButton("CONFIRM", (dialog, which) -> {
                    String newEmail = editEmail.getText().toString();

                    currEmail = newEmail;
                    saveUserEmail(newEmail);
                })
                .create()
                .show();
    }

    /**
     * Helper for adding phone number
     */
    private void addPNumberDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View pnumberView = inflater.inflate(R.layout.fragment_pnumber, null);
        EditText inputPNumber = pnumberView.findViewById(R.id.input_add_pnumber);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder
                .setView(pnumberView)
                .setNeutralButton("CANCEL", null)
                .setPositiveButton("CONFIRM", (dialog, which) -> {
                    String newPNumber = inputPNumber.getText().toString();

                    currPNumber = newPNumber;
                    saveUserPNumber(newPNumber);
                })
                .create()
                .show();
    }

    private void editPNumberDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View pnumberView = inflater.inflate(R.layout.fragment_pnumber, null);
        EditText editPNumber = pnumberView.findViewById(R.id.input_edit_pnumber);

        pnumberView.findViewById(R.id.input_add_pnumber).setVisibility(View.INVISIBLE);
        pnumberView.findViewById(R.id.input_edit_pnumber).setVisibility(View.VISIBLE);

        pnumberView.findViewById(R.id.title_add_pnumber).setVisibility(View.INVISIBLE);
        pnumberView.findViewById(R.id.title_edit_pnumber).setVisibility(View.VISIBLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder
                .setView(pnumberView)
                .setNeutralButton("CANCEL", null)
                .setPositiveButton("CONFIRM", (dialog, which) -> {
                    String newPNumber = editPNumber.getText().toString();

                    currPNumber = newPNumber;
                    saveUserPNumber(newPNumber);
                })
                .create()
                .show();
    }

    private void updatePhoneNumberButtonVisibility(View view, String phoneNumber) {
        View addButton = view.findViewById(R.id.add_pnumber_button);
        View editButton = view.findViewById(R.id.edit_pnumber_button);

        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            addButton.setVisibility(View.INVISIBLE);
            editButton.setVisibility(View.VISIBLE);
        } else {
            addButton.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Helper for setting up admin button
     */
    private void setupAdminButton(View view, Boolean isAdmin) {
        Button adminButton = view.findViewById(R.id.admin_button);
        if (isAdmin != null && isAdmin) {
            adminButton.setVisibility(View.VISIBLE);
            adminButton.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AdminActivity.class);
                startActivity(intent);
            });
        } else {
            adminButton.setVisibility(View.GONE);
        }
    }
}