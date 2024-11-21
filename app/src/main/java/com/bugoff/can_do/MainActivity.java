package com.bugoff.can_do;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.database.UserAuthenticator;
import com.bugoff.can_do.user.QrCodeScannerFragment;
import com.bugoff.can_do.user.UserViewModel;
import com.bugoff.can_do.user.UserViewModelFactory;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Main activity for the app. Handles navigation between different fragments using a BottomNavigationView.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private UserViewModel userViewModel;
    private GlobalRepository repository;

    /**
     * Initializes the activity and sets up the BottomNavigationView for navigation between different fragments.
     *
     * @param savedInstanceState The saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize repository - this can be either real or mock repository
        repository = new GlobalRepository();

        // Get Android ID
        @SuppressLint("HardwareIds") String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        UserAuthenticator.authenticateUser(androidId)
                .addOnSuccessListener(user -> {
                    if (user.getName() == null || user.getName().isEmpty()) {
                        setContentView(R.layout.activity_sign_in);
                        Log.d(TAG, "New User: " + user.getId());
                        initializeSignInScreen(user.getId());
                    } else {
                        // Existing user: proceed to load user data
                        setContentView(R.layout.activity_main);
                        Log.d(TAG, "Authenticated User: " + user.getId());

                        // Create UserViewModel using factory
                        UserViewModelFactory factory = new UserViewModelFactory(user.getId(), repository);
                        userViewModel = new ViewModelProvider(this, factory).get(UserViewModel.class);

                        GlobalRepository.setLoggedInUser(user);

                        if (savedInstanceState == null) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, new HomeActivity())
                                    .commit();
                        }

                        setupBottomNavigation();
                    }
                });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment selectedFragment = null;

            if (id == R.id.nav_home) {
                Log.d(TAG, "Home clicked");
                selectedFragment = new HomeActivity();
            } else if (id == R.id.nav_scan) {
                Log.d(TAG, "Scan Activity clicked");
                selectedFragment = new QrCodeScannerFragment();
            } else if (id == R.id.nav_profile) {
                Log.d(TAG, "Profile clicked");
                selectedFragment = new UserProfileActivity();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }

    /**
     * Initializes the sign-in screen by setting up the UI elements and handling the submit button click.
     */
    private void initializeSignInScreen(String userId) {
        EditText nameEditText = findViewById(R.id.nameEditText);
        EditText emailEditText = findViewById(R.id.emailEditText);

        findViewById(R.id.submitButton).setOnClickListener(view -> {
            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (email.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            UserAuthenticator.authenticateUser(userId).addOnSuccessListener(user -> {
                user.setName(name);
                user.setEmail(email);
                Task<Void> addUserTask = repository.addUser(user);  // Use instance repository
                addUserTask
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(MainActivity.this, "Welcome " + name + "!", Toast.LENGTH_SHORT).show();
                            switchToHomeScreen();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to save user to database", e);
                            Toast.makeText(MainActivity.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                        });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "User authentication failed", e);
                Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
            });
        });
    }

    /**
     * Switches to the home screen activity.
     */
    private void switchToHomeScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public UserViewModel getUserViewModel() {
        return userViewModel;
    }

    // For testing purposes
    public void setRepository(GlobalRepository repository) {
        this.repository = repository;
    }

    @VisibleForTesting
    public GlobalRepository getRepository() {
        return repository;
    }
}