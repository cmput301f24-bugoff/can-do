package com.bugoff.can_do.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bugoff.can_do.HomeActivity;
import com.bugoff.can_do.QrCodeScannerFragment;
import com.bugoff.can_do.R;
import com.bugoff.can_do.UserProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class OrganizerMain extends AppCompatActivity {
    private static final String TAG = "OrganizerMain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_organizer);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Fragment selectedFragmentOrganizer = null;

                if (id == R.id.nav_home_organizer) {
                    Log.d(TAG, "Home clicked");
                    // Replace with your home fragment or activity
                    return true;
                } else if (id == R.id.nav_events_organizer) {
                    Log.d(TAG, "Events clicked");
                    selectedFragmentOrganizer = new EventsFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_organizer, selectedFragmentOrganizer).commit();
                    return true;
                } else if (id == R.id.nav_profile_organizer) {
                    Log.d(TAG, "Profile clicked");
                    selectedFragmentOrganizer = new ProfileFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_organizer, selectedFragmentOrganizer).commit();
                    return true;
                } else {
                    return false;
                }
            }
        });

    }
}