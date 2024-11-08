package com.bugoff.can_do;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;

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

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firestore
        new GlobalRepository();

        // Get Android ID for user identification
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Authenticate user and check if name exists
        UserAuthenticator.authenticateUser(androidId)
                .addOnSuccessListener(user -> {
                    if (user.getName() == null || user.getName().isEmpty()) {
                        // Redirect to SignInActivity for new users
                        Intent intent = new Intent(MainActivity.this, signInActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Existing user: proceed to load user data
                        Log.d(TAG, "Authenticated User: " + user.getId());
                        userViewModel = new ViewModelProvider(this, new UserViewModelFactory(user.getId()))
                                .get(UserViewModel.class);
                        GlobalRepository.setLoggedInUser(user);

                        // Load the home fragment if no saved state
                        if (savedInstanceState == null) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, new HomeActivity())
                                    .commit();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Authentication failed", e));

        // Set up BottomNavigationView for navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Fragment selectedFragment = null;

                if (id == R.id.nav_home) {
                    selectedFragment = new HomeActivity();
                } else if (id == R.id.nav_scan) {
                    selectedFragment = new QrCodeScannerFragment();
                } else if (id == R.id.nav_profile) {
                    selectedFragment = new UserProfileActivity();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                    return true;
                }
                return false;
            }
        });
    }

    public UserViewModel getUserViewModel() {
        return userViewModel;
    }
}


