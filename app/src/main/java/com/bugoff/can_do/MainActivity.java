package com.bugoff.can_do;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.database.UserAuthenticator;
import com.bugoff.can_do.user.QrCodeScannerFragment;
import com.bugoff.can_do.user.UserViewModel;
import com.bugoff.can_do.user.UserViewModelFactory;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Main activity for the app. Handles navigation between different fragments using a BottomNavigationView.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private UserViewModel userViewModel;

    /**
     * Initializes the activity and sets up the BottomNavigationView for navigation between different fragments.
     *
     * @param savedInstanceState The saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Connect to Firestore
        new GlobalRepository();
        // Get Android ID
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        UserAuthenticator.authenticateUser(androidId)
                .addOnSuccessListener(user -> {
                    if (user.getName() == null || user.getName().isEmpty()) {
                        setContentView(R.layout.activity_sign_in);
                        Log.d(TAG, "New User: " + user.getId());
                        initializeSignInScreen();
                    } else {
                        // Existing user: proceed to load user data
                        setContentView(R.layout.activity_main);
                        Log.d(TAG, "Authenticated User: " + user.getId());
                        userViewModel = new ViewModelProvider(this, new UserViewModelFactory(user.getId()))
                                .get(UserViewModel.class);
                        GlobalRepository.setLoggedInUser(user);


                        if (savedInstanceState == null) {
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeActivity()).commit();
                        }

                        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
                        bottomNavigationView.setOnItemSelectedListener(item -> {
                            int id = item.getItemId();
                            Fragment selectedFragment = null;

                            if (id == R.id.nav_home) {
                                // Handle "Home" click
                                Log.d(TAG, "Home clicked");
                                selectedFragment = new HomeActivity();
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                                return true;
                            } else if (id == R.id.nav_scan) {
                                Log.d(TAG, "Scan Activity clicked");
                                selectedFragment = new QrCodeScannerFragment();
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                                return true;
                            } else if (id == R.id.nav_profile) {
                                // Handle "Profile" click
                                Log.d(TAG, "Profile clicked");
                                selectedFragment = new UserProfileActivity();
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                                return true;
                            } else {
                                return false;
                            }
                        });
                    }

                });
    }


    /**
     * Initializes the sign-in screen by setting up the UI elements and handling the submit button click.
     */
    private void initializeSignInScreen() {
        EditText nameEditText = findViewById(R.id.nameEditText);
        Button submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(view -> {
            String name = nameEditText.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }
            String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

            UserAuthenticator.authenticateUser(androidId).addOnSuccessListener(user -> {
                user.setName(name);
                GlobalRepository.addUser(user).addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Welcome " + name + "!", Toast.LENGTH_SHORT).show();
                    switchToHomeScreen();
                }).addOnFailureListener(e -> Log.e(TAG, "Failed to save user to database", e));
            }).addOnFailureListener(e -> Log.e(TAG, "User authentication failed", e));
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
}
