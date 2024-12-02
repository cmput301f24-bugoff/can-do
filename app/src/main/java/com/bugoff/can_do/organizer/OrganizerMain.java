package com.bugoff.can_do.organizer;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bugoff.can_do.R;
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.event.EventsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Main activity for the organizer module.
 * Handles navigation and fragment transactions for different sections such as Events and Profile.
 */
public class OrganizerMain extends AppCompatActivity {
    private static final String TAG = "OrganizerMain";
    private GlobalRepository repository;

    /**
     * Called when the activity is first created.
     * Initializes the repository and sets up the default fragment if none exists in savedInstanceState.
     *
     * @param savedInstanceState If the activity is being reinitialized after previously being shut down,
     *                           this Bundle contains the most recent data. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer);

        // Initialize the repository
        repository = new GlobalRepository();

        // Load the default fragment if no saved instance state exists
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new EventsFragment())
                    .commit();
        }

        // Set up bottom navigation
        setupBottomNavigation();
    }

    /**
     * Configures the bottom navigation bar to handle fragment switching
     * based on the selected menu item.
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_organizer);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment selectedFragmentOrganizer;

            // Determine which fragment to display based on the selected menu item
            if (id == R.id.nav_events_organizer) {
                Log.d(TAG, "Events clicked");
                selectedFragmentOrganizer = new EventsFragment();
            } else if (id == R.id.nav_profile_organizer) {
                Log.d(TAG, "Profile clicked");
                selectedFragmentOrganizer = new ProfileFragment();
            } else {
                return false;
            }

            // Replace the current fragment with the selected fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragmentOrganizer)
                    .commit();
            return true;
        });
    }

    /**
     * Allows setting a custom repository for testing purposes.
     *
     * @param repository The new GlobalRepository instance to use in this activity.
     */
    @VisibleForTesting
    public void setRepository(GlobalRepository repository) {
        this.repository = repository;
    }

    /**
     * Retrieves the current repository instance.
     *
     * @return The GlobalRepository instance used by this activity.
     */
    public GlobalRepository getRepository() {
        return repository;
    }
}
