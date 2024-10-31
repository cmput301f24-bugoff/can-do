package com.bugoff.can_do;

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
import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.user.User;
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

        // Connect to Firestore
        new GlobalRepository();

        // Get Android ID
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        UserAuthenticator.authenticateUser(androidId)
                .addOnSuccessListener(user -> {
                    // Handle successful authentication
                    Log.d(TAG, "Authenticated User: " + user.getId());

                    // Initialize UserViewModel with authenticated user ID
                    userViewModel = new ViewModelProvider(this, new UserViewModelFactory(user.getId()))
                            .get(UserViewModel.class);

                    GlobalRepository.setLoggedInUser(user);
                })
                .addOnFailureListener(e -> {
                    // Handle authentication failure
                    Log.e(TAG, "Authentication failed", e);
                });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeActivity()).commit();
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
            }
        });
    }

    public UserViewModel getUserViewModel() {
        return userViewModel;
    }

    public void databaseDemo(User user) {
        // Create a facility
        Facility facility = new Facility(user);
        facility.setRemote();
        facility.attachListener();
        // Create an event
        Event event = new Event(facility);
        event.setRemote();
        event.attachListener();
        Event event2 = new Event(facility);
        event2.setRemote();
        event2.attachListener();
    }
}
