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
 * Main activity for the organizer user type.
 * This activity displays the main screen for organizers, which includes a list of events and the user's profile.
 */
public class OrganizerMain extends AppCompatActivity {
    private static final String TAG = "OrganizerMain";
    private GlobalRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer);

        repository = new GlobalRepository();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new EventsFragment())
                    .commit();
        }

        setupBottomNavigation();
    }
    /**
     * Sets up the bottom navigation bar for switching between events and profile screens.
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_organizer);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment selectedFragmentOrganizer;

            if (id == R.id.nav_events_organizer) {
                Log.d(TAG, "Events clicked");
                selectedFragmentOrganizer = new EventsFragment();
            } else if (id == R.id.nav_profile_organizer) {
                Log.d(TAG, "Profile clicked");
                selectedFragmentOrganizer = new ProfileFragment();
            } else {
                return false;
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragmentOrganizer)
                    .commit();
            return true;
        });
    }

    @VisibleForTesting
    public void setRepository(GlobalRepository repository) {
        this.repository = repository;
    }

    public GlobalRepository getRepository() {
        return repository;
    }
}