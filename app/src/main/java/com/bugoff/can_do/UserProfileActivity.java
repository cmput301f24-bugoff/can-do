package com.bugoff.can_do;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
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
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.notification.NotificationSettingsActivity;
import com.bugoff.can_do.organizer.OrganizerTransition;
import com.bugoff.can_do.user.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

/**
 * Fragment for the user profile screen.
 */
public class UserProfileActivity extends Fragment {
    private User user;
    private String currEmail;
    private String currPNumber;

    /**
     * Initializes the user profile screen and sets up the buttons for editing user information.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment
     * @param container          The parent view that the fragment's UI should be attached to
     * @param savedInstanceState The previously saved state of the fragment
     * @return The view for the user profile screen
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_profile_screen, container, false);

        String androidID = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        loadUserData(db, androidID);

        // User clicks "I'm an organizer"
        view.findViewById(R.id.organizer_button).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OrganizerTransition.class);
            startActivity(intent);
        });

        // User clicks "Notification Settings"
        view.findViewById(R.id.notif_button).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationSettingsActivity.class);
            startActivity(intent);
        });

        // User clicks "Edit Name"
        view.findViewById(R.id.name_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView firstName = view.findViewById(R.id.first_name);
                TextView lastName = view.findViewById(R.id.last_name);

                editNameDialog(firstName, lastName);
            }
        });

        // User clicks "Edit Email"
        view.findViewById(R.id.email_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editEmailDialog();
            }
        });

        // User clicks "Add Phone Number", only visible if no phone number registered
        view.findViewById(R.id.add_pnumber_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { addPNumberDialog(); }
        });

        // User clicks "Edit Phone Number", becomes visible with existing phone number
        view.findViewById(R.id.edit_pnumber_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { editPNumberDialog(); }
        });

        setupAdminButton(view);

        // User clicks avatar to change or add profile picture
        view.findViewById(R.id.image_avatar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Change Profile Picture")
                        .setMessage("Choose a source")
                        .setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                galleryLauncher.launch(galleryIntent);
                            }
                        })
                        .setNegativeButton("Camera", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                cameraLauncher.launch(cameraIntent);
                            }
                        })
                        .setNeutralButton("Remove", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                TextView userNameTextView = view.findViewById(R.id.first_name);
                                String userName = userNameTextView.getText().toString();

                                String firstLetter = userName.isEmpty() ? "A" : userName.substring(0, 1).toUpperCase();

                                ImageView avatar = view.findViewById(R.id.image_avatar);
                                avatar.setImageBitmap(generateAvatar(firstLetter));
                            }
                        })
                        .show();
            }
        });

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
        if (user == null) {
            Toast.makeText(getContext(), "User data is not loaded yet", Toast.LENGTH_SHORT).show();
        }

        String name = firstNameTxt.getText().toString() + " " + lastNameTxt.getText().toString();

        user.setName(name);
        user.setRemote();

        // Automatically set profile picture to default avatar
        String avatarFirstName = firstNameTxt.getText().toString();
        String firstLetter = avatarFirstName.isEmpty() ? "A" : avatarFirstName.substring(0, 1).toUpperCase();
        ImageView avatar = getView().findViewById(R.id.image_avatar);
        avatar.setImageBitmap(generateAvatar(firstLetter));
    }

    private void saveUserEmail(String email) {
        if (user == null) {
            Toast.makeText(getContext(), "User data is not loaded yet", Toast.LENGTH_SHORT).show();
        }

        user.setEmail(email);
        user.setRemote();
    }

    private void saveUserPNumber(String pnumber) {
        if (user == null) {
            Toast.makeText(getContext(), "User data is not loaded yet", Toast.LENGTH_SHORT).show();
        }

        user.setPhoneNumber(pnumber);
        user.setRemote();
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

    /**
     * Helper for setting up admin button
     *
     * @param view Fragment view
     */
    private void setupAdminButton(View view) {
        Button adminButton = view.findViewById(R.id.admin_button);
        if (GlobalRepository.getLoggedInUser() != null && GlobalRepository.getLoggedInUser().getIsAdmin()) {
            adminButton.setVisibility(View.VISIBLE);
            adminButton.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AdminActivity.class);
                startActivity(intent);
            });
        } else {
            adminButton.setVisibility(View.GONE); // Hide if not admin
        }
    }
}