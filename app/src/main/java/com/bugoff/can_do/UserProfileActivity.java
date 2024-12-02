package com.bugoff.can_do;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bugoff.can_do.admin.AdminActivity;
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.notification.NotificationSettingsActivity;
import com.bugoff.can_do.organizer.OrganizerTransition;
import com.bugoff.can_do.user.User;
import com.bugoff.can_do.user.UserViewModel;
import com.bugoff.can_do.user.UserViewModelFactory;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Fragment for the user profile screen.
 */
public class UserProfileActivity extends Fragment {
    private User user;
    private UserViewModel userViewModel;
    private String currEmail;
    private String currPNumber;
    private Uri cameraPicUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the UserViewModel from the MainActivity
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            userViewModel = activity.getUserViewModel();

            if (userViewModel == null) {
                // If somehow ViewModel is not available from MainActivity, create it directly
                @SuppressLint("HardwareIds")
                String androidId = Settings.Secure.getString(requireActivity().getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                UserViewModelFactory factory = new UserViewModelFactory(androidId);
                userViewModel = new ViewModelProvider(this, factory).get(UserViewModel.class);
            }
        } else {
            Log.e("UserProfile", "Activity is not MainActivity");
            Toast.makeText(getContext(), "Error initializing profile", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_profile_screen, container, false);

        // Initialize views first
        ImageButton avatar = view.findViewById(R.id.image_avatar);
        TextView firstName = view.findViewById(R.id.first_name);
        TextView lastName = view.findViewById(R.id.last_name);

        // Set default or loading state
        firstName.setText("");
        lastName.setText("");

        if (userViewModel != null) {
            setupViewModelObservers(view, avatar, firstName, lastName);
        } else {
            Toast.makeText(getContext(), "Error loading profile data", Toast.LENGTH_SHORT).show();
        }

        setupClickListeners(view, firstName, lastName, avatar);
        return view;
    }
    /**
     * Helper for setting up ViewModel observers
     *
     * @param view     Fragment view
     * @param avatar   Avatar ImageButton
     * @param firstName First name TextView
     * @param lastName  Last name TextView
     */
    private void setupViewModelObservers(View view, ImageButton avatar, TextView firstName, TextView lastName) {
        userViewModel.getUserName().observe(getViewLifecycleOwner(), name -> {
            if (name != null) {
                String[] parts = name.split(" ");
                if (parts.length > 0) {
                    firstName.setText(parts[0]);
                    String firstLetter = parts[0].isEmpty() ? "A" : parts[0].substring(0, 1).toUpperCase();
                    loadUserProfileImage(avatar, firstLetter);
                }
                if (parts.length > 1) {
                    lastName.setText(parts[1]);
                } else {
                    lastName.setText("");
                }
            }
        });

        userViewModel.getEmail().observe(getViewLifecycleOwner(), email -> {
            currEmail = email != null ? email : "";
        });

        userViewModel.getPhoneNumber().observe(getViewLifecycleOwner(), phoneNumber -> {
            currPNumber = phoneNumber != null ? phoneNumber : "";
            updatePhoneNumberButtonVisibility(view, phoneNumber);
        });

        userViewModel.getIsAdmin().observe(getViewLifecycleOwner(), isAdmin -> {
            setupAdminButton(view, isAdmin);
        });

        userViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }
    /**
     * Helper for setting up click listeners for the profile screen
     *
     * @param view      Fragment view
     * @param firstName First name TextView
     * @param lastName  Last name TextView
     * @param avatar    Avatar ImageButton
     */
    private void setupClickListeners(View view, TextView firstName, TextView lastName, ImageButton avatar) {
        view.findViewById(R.id.name_button).setOnClickListener(v ->
                editNameDialog(firstName, lastName));

        view.findViewById(R.id.email_button).setOnClickListener(v ->
                editEmailDialog());

        view.findViewById(R.id.add_pnumber_button).setOnClickListener(v ->
                addPNumberDialog());

        view.findViewById(R.id.edit_pnumber_button).setOnClickListener(v ->
                editPNumberDialog());

        view.findViewById(R.id.notif_button).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationSettingsActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.organizer_button).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OrganizerTransition.class);
            startActivity(intent);
        });

        // Set up avatar click listener
        setupAvatarClickListener(view);
    }
    /**
     * Helper for setting up avatar click listener
     *
     * @param view Fragment view
     */
    private void setupAvatarClickListener(View view) {
        ImageView avatar = view.findViewById(R.id.image_avatar);
        avatar.setOnClickListener(v -> {
            TextView firstName = view.findViewById(R.id.first_name);
            String userName = firstName.getText().toString();
            String firstLetter = userName.isEmpty() ? "A" : userName.substring(0, 1).toUpperCase();

            new AlertDialog.Builder(requireContext())
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
                        User currentUser = GlobalRepository.getLoggedInUser();
                        if (currentUser != null) {
                            currentUser.setBase64Image(null);
                            avatar.setImageBitmap(generateAvatar(firstLetter));
                        }
                    })
                    .show();
        });
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
     * Helper for saving avatar image to internal storage
     *
     * @param bitmap   Bitmap image to save
     * @param filename Filename to save as
     * @return Absolute path of saved image
     */
    private String saveAvatar(Bitmap bitmap, String filename) {
        try {
            FileOutputStream fos = requireContext().openFileOutput(filename, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            return new File(requireContext().getFilesDir(), filename).getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to save image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    ImageView avatar = getView().findViewById(R.id.image_avatar);

                    // Compress and save image
                    String base64Image = ImageUtils.compressAndEncodeImage(requireContext(), selectedImageUri);
                    if (base64Image != null) {
                        User currentUser = GlobalRepository.getLoggedInUser();
                        currentUser.setBase64Image(base64Image);

                        // Update ImageView
                        Bitmap bitmap = ImageUtils.decodeBase64Image(base64Image);
                        if (bitmap != null) {
                            avatar.setImageBitmap(bitmap);
                        }
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
                        Bitmap photo = (Bitmap) extras.get("data");
                        if (photo != null) {
                            ImageView avatar = getView().findViewById(R.id.image_avatar);

                            // Compress and save image
                            String base64Image = ImageUtils.compressAndEncodeBitmap(photo);
                            if (base64Image != null) {
                                User currentUser = GlobalRepository.getLoggedInUser();
                                currentUser.setBase64Image(base64Image);

                                // Update ImageView
                                avatar.setImageBitmap(photo);
                            }
                        }
                    }
                }
            }
    );
    /**
     * Helper for loading user profile image
     *
     * @param avatar      Avatar ImageView
     * @param firstLetter First letter of user's name
     */
    private void loadUserProfileImage(ImageView avatar, String firstLetter) {
        User currentUser = GlobalRepository.getLoggedInUser();
        if (currentUser != null && currentUser.getBase64Image() != null) {
            Bitmap bitmap = ImageUtils.decodeBase64Image(currentUser.getBase64Image());
            if (bitmap != null) {
                avatar.setImageBitmap(bitmap);
            } else {
                avatar.setImageBitmap(generateAvatar(firstLetter));
            }
        } else {
            avatar.setImageBitmap(generateAvatar(firstLetter));
        }
    }

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
 * Opens a dialog to edit the user's name.
 *
 * @param firstName The TextView displaying the user's first name
 * @param lastName  The TextView displaying the user's last name
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

        new AlertDialog.Builder(getContext())
                .setView(nameView)
                .setNeutralButton("CANCEL", null)
                .setPositiveButton("CONFIRM", (dialog, which) -> {
                    String newFirstName = editFirstName.getText().toString().trim();
                    String newLastName = editLastName.getText().toString().trim();
                    String fullName = newFirstName;
                    if (!newLastName.isEmpty()) {
                        fullName += " " + newLastName;
                    }
                    userViewModel.setName(fullName);
                })
                .create()
                .show();
    }

    /**
     * Opens a dialog to edit the user's email.
     */
    private void editEmailDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View emailView = inflater.inflate(R.layout.fragment_edit_email, null);

        TextView oldEmail = emailView.findViewById(R.id.textview_old_email);
        EditText editEmail = emailView.findViewById(R.id.input_new_email);

        oldEmail.setText(currEmail);

        new AlertDialog.Builder(getContext())
                .setView(emailView)
                .setNeutralButton("CANCEL", null)
                .setPositiveButton("CONFIRM", (dialog, which) -> {
                    String newEmail = editEmail.getText().toString().trim();
                    if (!newEmail.isEmpty()) {
                        userViewModel.setEmail(newEmail);
                    }
                })
                .create()
                .show();
    }
    /**
     * Opens a dialog to add a phone number to the user's profile.
     */
    private void addPNumberDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View pnumberView = inflater.inflate(R.layout.fragment_pnumber, null);
        EditText inputPNumber = pnumberView.findViewById(R.id.input_add_pnumber);

        new AlertDialog.Builder(getContext())
                .setView(pnumberView)
                .setNeutralButton("CANCEL", null)
                .setPositiveButton("CONFIRM", (dialog, which) -> {
                    String newPNumber = inputPNumber.getText().toString().trim();
                    if (!newPNumber.isEmpty()) {
                        userViewModel.setPhoneNumber(newPNumber);
                    }
                })
                .create()
                .show();
    }
    /**
     * Opens a dialog to edit the user's phone number.
     */
    private void editPNumberDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View pnumberView = inflater.inflate(R.layout.fragment_pnumber, null);
        EditText editPNumber = pnumberView.findViewById(R.id.input_edit_pnumber);

        pnumberView.findViewById(R.id.input_add_pnumber).setVisibility(View.INVISIBLE);
        pnumberView.findViewById(R.id.input_edit_pnumber).setVisibility(View.VISIBLE);

        pnumberView.findViewById(R.id.title_add_pnumber).setVisibility(View.INVISIBLE);
        pnumberView.findViewById(R.id.title_edit_pnumber).setVisibility(View.VISIBLE);

        editPNumber.setText(currPNumber);

        new AlertDialog.Builder(getContext())
                .setView(pnumberView)
                .setNeutralButton("CANCEL", null)
                .setPositiveButton("CONFIRM", (dialog, which) -> {
                    String newPNumber = editPNumber.getText().toString().trim();
                    if (!newPNumber.isEmpty()) {
                        userViewModel.setPhoneNumber(newPNumber);
                    }
                })
                .create()
                .show();
    }
    /**
     * Helper for updating phone number button visibility
     *
     * @param view       Fragment view
     * @param phoneNumber Phone number string
     */
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