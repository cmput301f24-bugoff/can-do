package com.bugoff.can_do.organizer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bugoff.can_do.ImageUtils;
import com.bugoff.can_do.MainActivity;
import com.bugoff.can_do.R;
import com.bugoff.can_do.notification.NotificationSettingsActivity;
import com.bugoff.can_do.user.User;
import com.bugoff.can_do.user.UserViewModel;
import com.bugoff.can_do.user.UserViewModelFactory;
/**
 * Fragment for displaying and managing the user's profile in the organizer mode.
 *
 * <p>This fragment allows users to view and update their profile details, such as
 * name, email, phone number, and avatar image. It also provides navigation to other
 * activities, including notification settings, facility management, and switching to attendee mode.</p>
 */
public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private UserViewModel userViewModel;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ImageView avatarImageView;
    private TextView firstNameTextView;
    private TextView lastNameTextView;
    private String currentEmail = "";
    private String currentPhone = "";
    /**
     * Called to initialize the fragment and set up the ViewModel and image launchers.
     *
     * @param savedInstanceState The saved state of the fragment, if any.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupImageLaunchers();

        // Initialize ViewModel
        @SuppressLint("HardwareIds") String androidId = Settings.Secure.getString(
                requireActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
        UserViewModelFactory factory = new UserViewModelFactory(androidId);
        userViewModel = new ViewModelProvider(this, factory).get(UserViewModel.class);
    }
    /**
     * Called to create and return the view hierarchy for the fragment.
     *
     * @param inflater  The LayoutInflater used to inflate the fragment's layout.
     * @param container The parent container for the fragment's view.
     * @param savedInstanceState The saved state of the fragment, if any.
     * @return The root view of the fragment's layout.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_profile_organizer, container, false);
    }
    /**
     * Called after the fragment's view has been created.
     *
     * @param view               The root view of the fragment's layout.
     * @param savedInstanceState The saved state of the fragment, if any.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupObservers();
        setupClickListeners(view);
    }
    /**
     * Called after the fragment's view has been created.
     *
     * @param view               The root view of the fragment's layout.
     */
    private void initializeViews(View view) {
        avatarImageView = view.findViewById(R.id.image_avatar);
        firstNameTextView = view.findViewById(R.id.first_name);
        lastNameTextView = view.findViewById(R.id.last_name);
    }
    /**
     * Sets up observers for LiveData from the ViewModel to update the UI reactively.
     */
    private void setupObservers() {
        userViewModel.getUserName().observe(getViewLifecycleOwner(), name -> {
            if (name != null) {
                updateNameFields(name);
                // Update avatar with first letter when name changes
                String firstLetter = firstNameTextView.getText().toString();
                if (!firstLetter.isEmpty()) {
                    firstLetter = firstLetter.substring(0, 1).toUpperCase();
                    loadUserProfileImage(firstLetter);
                }
            }
        });

        userViewModel.getEmail().observe(getViewLifecycleOwner(), email -> {
            currentEmail = email != null ? email : "";
        });

        userViewModel.getPhoneNumber().observe(getViewLifecycleOwner(), phoneNumber -> {
            currentPhone = phoneNumber != null ? phoneNumber : "";
        });
    }
    /**
     * Sets up click listeners for various profile actions, such as editing name, email, and phone number.
     *
     * @param view The root view of the fragment's layout.
     */
    private void setupClickListeners(View view) {
        // Edit Name
        view.findViewById(R.id.name_button).setOnClickListener(v -> showEditNameDialog());

        // Edit Email
        view.findViewById(R.id.email_button).setOnClickListener(v -> showEditEmailDialog());

        // Phone Number
        view.findViewById(R.id.add_pnumber_button).setOnClickListener(v -> addOrEditPhoneNumberDialog());

        // Notification Settings
        view.findViewById(R.id.notif_button).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationSettingsActivity.class);
            startActivity(intent);
        });

        // Manage Facility
        view.findViewById(R.id.manage_facility_button).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FacilityEdit.class);
            startActivity(intent);
        });

        // Attendee Mode
        view.findViewById(R.id.attendee_button).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        });

        // Avatar
        avatarImageView.setOnClickListener(v -> showImageSourceDialog());
    }
    /**
     * Sets up the image launchers for selecting or capturing a profile picture.
     */
    private void setupImageLaunchers() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        handleGalleryImage(selectedImageUri);
                    }
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null && extras.get("data") instanceof Bitmap) {
                            handleCameraImage((Bitmap) extras.get("data"));
                        }
                    }
                }
        );
    }
    /**
     * Handles an image selected from the gallery.
     *
     * @param imageUri The URI of the selected image.
     */
    private void handleGalleryImage(Uri imageUri) {
        if (imageUri != null) {
            String base64Image = ImageUtils.compressAndEncodeImage(requireContext(), imageUri);
            if (base64Image != null) {
                updateUserProfileImage(base64Image);
            }
        }
    }
    /**
     * Handles an image captured from the camera.
     *
     * @param photo The captured image as a Bitmap.
     */
    private void handleCameraImage(Bitmap photo) {
        String base64Image = ImageUtils.compressAndEncodeBitmap(photo);
        if (base64Image != null) {
            updateUserProfileImage(base64Image);
        }
    }
    /**
     * Updates the user's profile picture with a given base64-encoded image.
     *
     * @param base64Image The base64-encoded image string.
     */
    private void updateUserProfileImage(String base64Image) {
        User currentUser = getViewModel().getUser();
        if (currentUser != null) {
            currentUser.setBase64Image(base64Image);
            Bitmap bitmap = ImageUtils.decodeBase64Image(base64Image);
            if (bitmap != null) {
                avatarImageView.setImageBitmap(bitmap);
            }
        }
    }
    /**
     * Displays a dialog to select the image source for the profile picture.
     */
    private void showImageSourceDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Change Profile Picture")
                .setMessage("Choose a source")
                .setPositiveButton("Gallery", (dialog, which) -> {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryLauncher.launch(galleryIntent);
                })
                .setNegativeButton("Camera", (dialog, which) -> {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraLauncher.launch(cameraIntent);
                })
                .setNeutralButton("Remove", (dialog, which) -> removeProfileImage())
                .show();
    }
    /**
     * Removes the user's profile picture and updates the UI with a default avatar.
     */
    private void removeProfileImage() {
        User currentUser = getViewModel().getUser();
        if (currentUser != null) {
            currentUser.setBase64Image(null);
            String firstLetter = getFirstLetterOfName();
            loadUserProfileImage(firstLetter);
        }
    }
    /**
     * Loads the user's profile picture from the base64-encoded image or generates a default avatar.
     *
     * @param firstLetter The first letter of the user's name.
     */
    private void loadUserProfileImage(String firstLetter) {
        User currentUser = getViewModel().getUser();
        if (currentUser != null && currentUser.getBase64Image() != null) {
            Bitmap bitmap = ImageUtils.decodeBase64Image(currentUser.getBase64Image());
            if (bitmap != null) {
                avatarImageView.setImageBitmap(bitmap);
                return;
            }
        }
        avatarImageView.setImageBitmap(ImageUtils.generateDefaultAvatar(firstLetter));
    }
    /**
     * Updates the name fields (first and last name) in the UI.
     *
     * @param fullName The full name to split into first and last name.
     */
    private void updateNameFields(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            firstNameTextView.setText("");
            lastNameTextView.setText("");
            return;
        }

        String[] parts = fullName.trim().split("\\s+", 2);
        firstNameTextView.setText(parts[0]);
        lastNameTextView.setText(parts.length > 1 ? parts[1] : "");
    }
    /**
     * Displays a dialog to edit the user's name.
     */
    private void showEditNameDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.fragment_edit_name, null);
        EditText firstNameInput = dialogView.findViewById(R.id.input_first_name);
        EditText lastNameInput = dialogView.findViewById(R.id.input_last_name);

        // Pre-fill current values
        firstNameInput.setText(firstNameTextView.getText());
        lastNameInput.setText(lastNameTextView.getText());

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setNeutralButton("CANCEL", null)
                .setPositiveButton("CONFIRM", null) // Set to null initially
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String newFirstName = firstNameInput.getText().toString().trim();
                String newLastName = lastNameInput.getText().toString().trim();

                if (newFirstName.isEmpty()) {
                    firstNameInput.setError("First name cannot be empty");
                    return;
                }

                StringBuilder fullName = new StringBuilder(newFirstName);
                if (!newLastName.isEmpty()) {
                    fullName.append(" ").append(newLastName);
                }

                userViewModel.setName(fullName.toString());
                dialog.dismiss();
            });
        });

        dialog.show();
    }
    /**
     * Displays a dialog to edit the user's email address.
     *
     * <p>The dialog pre-fills the current email and allows the user to input a new email.
     * If the input is valid and not empty, the new email is updated in the {@link UserViewModel}.</p>
     */
    private void showEditEmailDialog() {
        View emailView = getLayoutInflater().inflate(R.layout.fragment_edit_email, null);
        TextView oldEmail = emailView.findViewById(R.id.textview_old_email);
        EditText editEmail = emailView.findViewById(R.id.input_new_email);

        oldEmail.setText(currentEmail);

        new AlertDialog.Builder(requireContext())
                .setView(emailView)
                .setNeutralButton("CANCEL", null)
                .setPositiveButton("CONFIRM", (dialog, which) -> {
                    String newEmail = editEmail.getText().toString().trim();
                    if (!newEmail.isEmpty() && userViewModel != null) {
                        userViewModel.setEmail(newEmail);
                    }
                })
                .create()
                .show();
    }
    /**
     * Displays a dialog to add or edit the user's phone number.
     *
     * <p>The dialog dynamically adjusts to show either an "Add Phone Number" or "Edit Phone Number"
     * interface based on whether a phone number already exists. The input is validated before
     * updating the {@link UserViewModel}.</p>
     */
    private void addOrEditPhoneNumberDialog() {
        View pnumberView = getLayoutInflater().inflate(R.layout.fragment_pnumber, null);

        // Get references to all views we need to manage
        View addLayout = pnumberView.findViewById(R.id.input_add_pnumber);
        View editLayout = pnumberView.findViewById(R.id.input_edit_pnumber);
        View addTitle = pnumberView.findViewById(R.id.title_add_pnumber);
        View editTitle = pnumberView.findViewById(R.id.title_edit_pnumber);
        EditText inputPNumber = pnumberView.findViewById(R.id.input_add_pnumber);
        EditText editPNumber = pnumberView.findViewById(R.id.input_edit_pnumber);

        boolean isEditing = currentPhone != null && !currentPhone.isEmpty();

        // Show/hide appropriate views
        addLayout.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        editLayout.setVisibility(isEditing ? View.VISIBLE : View.GONE);
        addTitle.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        editTitle.setVisibility(isEditing ? View.VISIBLE : View.GONE);

        // Set current phone number if editing
        if (isEditing) {
            editPNumber.setText(currentPhone);
        }

        new AlertDialog.Builder(requireContext())
                .setView(pnumberView)
                .setNeutralButton("CANCEL", null)
                .setPositiveButton("CONFIRM", (dialog, which) -> {
                    String newNumber = isEditing ?
                            editPNumber.getText().toString().trim() :
                            inputPNumber.getText().toString().trim();
                    if (!newNumber.isEmpty()) {
                        userViewModel.setPhoneNumber(newNumber);
                    }
                })
                .create()
                .show();
    }
    /**
     * Retrieves the first letter of the user's first name.
     *
     * @return The first letter, or "A" if the name is empty.
     */
    private String getFirstLetterOfName() {
        String firstName = firstNameTextView.getText().toString();
        return !firstName.isEmpty() ? firstName.substring(0, 1).toUpperCase() : "A";
    }
    /**
     * Gets the current {@link UserViewModel}, initializing it if necessary.
     *
     * @return The current UserViewModel instance.
     */
    private UserViewModel getViewModel() {
        if (userViewModel == null) {
            @SuppressLint("HardwareIds") String androidId = Settings.Secure.getString(
                    requireActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
            UserViewModelFactory factory = new UserViewModelFactory(androidId);
            userViewModel = new ViewModelProvider(this, factory).get(UserViewModel.class);
        }
        return userViewModel;
    }
}