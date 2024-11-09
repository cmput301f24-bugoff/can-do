package com.bugoff.can_do.organizer;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bugoff.can_do.R;
import com.bugoff.can_do.event.EventsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Main activity for the organizer section of the app. Handles navigation between fragments using a
 * BottomNavigationView. This activity allows users to switch between the 'Events' and 'Profile' sections.
 */
public class OrganizerMain extends AppCompatActivity {
    private static final String TAG = "OrganizerMain";

    /**
     * Called when the activity is first created. Initializes the BottomNavigationView and sets up
     * the item selection listener to handle fragment navigation.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the most recent data. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new EventsFragment()).commit();
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_organizer);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment selectedFragmentOrganizer;

            if (id == R.id.nav_events_organizer) {
                Log.d(TAG, "Events clicked");
                selectedFragmentOrganizer = new EventsFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragmentOrganizer).commit();
                return true;
            } else if (id == R.id.nav_profile_organizer) {
                Log.d(TAG, "Profile clicked");
                selectedFragmentOrganizer = new ProfileFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragmentOrganizer).commit();
                return true;
            } else {
                return false;
            }
        });

    }
}
