package com.bugoff.can_do.organizer;

import android.annotation.SuppressLint;
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
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bugoff.can_do.ImageUtils;
import com.bugoff.can_do.MainActivity;
import com.bugoff.can_do.R;
import com.bugoff.can_do.notification.NotificationSettingsActivity;
import com.bugoff.can_do.user.User;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A Fragment representing the user's profile in the organizer context. This fragment allows the user
 * to either navigate to the home screen as an attendee or manage their facility.
 */
public class ProfileFragment extends Fragment {
    private User user;
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

        TextView userNameTextView = view.findViewById(R.id.first_name);
        String userName = userNameTextView.getText().toString();

        @SuppressLint("HardwareIds") String androidID = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        loadUserData(db, androidID);

        // If user clicks "Edit Name"
        view.findViewById(R.id.name_button).setOnClickListener(v -> {
            TextView lastName = view.findViewById(R.id.last_name);
            editNameDialog(userNameTextView, lastName);
        });

        // If user clicks "Edit Email"
        view.findViewById(R.id.email_button).setOnClickListener(v -> editEmailDialog());

        // If user clicks "Add Phone Number"
        view.findViewById(R.id.add_pnumber_button).setOnClickListener(v -> addOrEditPhoneNumberDialog());

        // User clicks "Notification Settings"
        view.findViewById(R.id.notif_button).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationSettingsActivity.class);
            startActivity(intent);
        });

        // If user clicks "Manage Facility"
        view.findViewById(R.id.manage_facility_button).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FacilityEdit.class);
            startActivity(intent);
        });

        // If user clicks "I'm an Attendee"
        view.findViewById(R.id.attendee_button).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.image_avatar).setOnClickListener(v -> {
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
                        user.setBase64Image(null);
                        ImageView avatar = getView().findViewById(R.id.image_avatar);
                        String firstLetter = user.getName() != null && !user.getName().isEmpty()
                                ? user.getName().substring(0, 1).toUpperCase()
                                : "A";
                        avatar.setImageBitmap(generateAvatar(firstLetter));
                        user.setRemote(); // Save changes to Firestore
                    })
                    .show();
        });


        return view;
    }

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
     * Loads the user's profile image from base64 or sets a default avatar.
     *
     * @param avatar      The ImageView to display the avatar.
     * @param firstLetter The first letter of the user's name for the default avatar.
     */
    private void loadUserProfileImage(ImageView avatar, String firstLetter) {
        if (user != null && user.getBase64Image() != null) {
            Bitmap bitmap = ImageUtils.decodeBase64Image(user.getBase64Image());
            if (bitmap != null) {
                avatar.setImageBitmap(bitmap);
            } else {
                avatar.setImageBitmap(generateAvatar(firstLetter));
            }
        } else {
            avatar.setImageBitmap(generateAvatar(firstLetter));
        }
    }

    private ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    ImageView avatar = getView().findViewById(R.id.image_avatar);

                    // Compress and encode image to base64
                    String base64Image = ImageUtils.compressAndEncodeImage(requireContext(), selectedImageUri);
                    if (base64Image != null) {
                        user.setBase64Image(base64Image);
                        avatar.setImageBitmap(ImageUtils.decodeBase64Image(base64Image));
                        user.setRemote(); // Save changes to Firestore
                    }
                }
            }
    );


    private ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap photo = (Bitmap) extras.get("data"); // Corrected key to "data"
                        if (photo != null) {
                            ImageView avatar = getView().findViewById(R.id.image_avatar);

                            // Compress and encode bitmap to base64
                            String base64Image = ImageUtils.compressAndEncodeBitmap(photo);
                            if (base64Image != null) {
                                user.setBase64Image(base64Image);
                                avatar.setImageBitmap(photo);
                                user.setRemote(); // Save changes to Firestore
                            }
                        }
                    }
                }
            }
    );


    private void loadUserData(FirebaseFirestore db, String androidID) {
        db.collection("users").document(androidID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        user = new User(documentSnapshot);
                        String userName = user.getName();
                        TextView firstName = getView().findViewById(R.id.first_name);
                        TextView lastName = getView().findViewById(R.id.last_name);
                        ImageView avatar = getView().findViewById(R.id.image_avatar); // Ensure avatar is defined here

                        if (userName != null && !userName.isEmpty()) {
                            String[] parts = userName.split(" ");
                            if (parts.length > 0) {
                                firstName.setText(parts[0]);
                            }
                            if (parts.length > 1) {
                                lastName.setText(parts[1]);
                            }

                            // Load profile image using base64
                            String firstLetter = parts[0].isEmpty() ? "A" : parts[0].substring(0, 1).toUpperCase();
                            loadUserProfileImage(avatar, firstLetter);
                        }
                    } else {
                        user = new User(androidID);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failure to load data", Toast.LENGTH_SHORT).show());
    }


    private void saveUserData(TextView firstNameTxt, TextView lastNameTxt) {
        if (user == null) {
            Toast.makeText(getContext(), "User data is not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }
        String name = firstNameTxt.getText().toString() + " " + lastNameTxt.getText().toString();
        user.setName(name);
        user.setRemote();
    }

    private void editNameDialog(TextView firstName, TextView lastName) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View nameView = inflater.inflate(R.layout.fragment_edit_name, null);

        String first = firstName.getText().toString();
        String last = lastName.getText().toString();

        EditText editFirstName = nameView.findViewById(R.id.input_first_name);
        EditText editLastName = nameView.findViewById(R.id.input_last_name);

        editFirstName.setText(first);
        editLastName.setText(last);

        new AlertDialog.Builder(getContext())
                .setView(nameView)
                .setNeutralButton("CANCEL", null)
                .setPositiveButton("CONFIRM", (dialog, which) -> {
                    firstName.setText(editFirstName.getText().toString());
                    lastName.setText(editLastName.getText().toString());
                    saveUserData(firstName, lastName);
                })
                .create()
                .show();
    }

    private void editEmailDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View emailView = inflater.inflate(R.layout.fragment_edit_email, null);

        new AlertDialog.Builder(getContext())
                .setView(emailView)
                .setNeutralButton("CANCEL", null)
                .setPositiveButton("CONFIRM", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void addOrEditPhoneNumberDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View pnumberView = inflater.inflate(R.layout.fragment_pnumber, null);

        new AlertDialog.Builder(getContext())
                .setView(pnumberView)
                .setNeutralButton("CANCEL", null)
                .setPositiveButton("CONFIRM", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
}
