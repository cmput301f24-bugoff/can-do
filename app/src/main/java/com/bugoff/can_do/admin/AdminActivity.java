package com.bugoff.can_do.admin;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bugoff.can_do.R;
import com.bugoff.can_do.event.EventsFragment;
import com.bugoff.can_do.organizer.ProfileFragment;
import com.bugoff.can_do.user.BrowseProfilesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * {@code AdminActivity} serves as the main interface for admin users,
 * allowing them to browse events, profiles, and images. It manages
 * fragment transactions to display the corresponding sections based on
 * the admin's interactions.
 */
public class AdminActivity extends AppCompatActivity {
    /**
     * Initializes the activity, sets the content view, and sets up the bottom navigation.
     *
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_admin);

        // Load the default fragment only once, without setting the bottom navigation item again
        if (savedInstanceState == null) {
            loadFragment(EventsFragment.newInstance(true));
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment;

            if (item.getItemId() == R.id.browse_events) {
                Log.d(TAG, "Browsing events");
                selectedFragment = EventsFragment.newInstance(true);
                loadFragment(selectedFragment);
            } else if (item.getItemId() == R.id.browse_profiles) {
                Log.d(TAG, "Browsing profiles");
                selectedFragment = new BrowseProfilesFragment();
                loadFragment(selectedFragment);
            } else if (item.getItemId() == R.id.browse_images) {
                Log.d(TAG, "Browsing images");
                selectedFragment = new BrowseImagesFragment();
            } else if (item.getItemId() == R.id.logout) {
                logOutUser();
                return true;
            } else {
                return false;
            }

            loadFragment(selectedFragment);
            return true;
        });
    }

    /**
     * Loads the specified fragment into the fragment container.
     *
     * @param fragment The fragment to load.
     */
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Handles the behavior when the Up button in the action bar is pressed.
     * If there are fragments in the back stack, it pops the last fragment.
     * Otherwise, it finishes the activity.
     *
     * @return {@code true} if the Up navigation was handled successfully, {@code false} otherwise.
     */
    @Override
    public boolean onSupportNavigateUp() {
        // Handle the Up button behavior
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            return true;
        }
        finish();
        return true;
    }

    /**
     * Logs out the user and finishes the activity.
     */
    private void logOutUser() {
        Log.d(TAG, "User logged out");
        finish();
    }
}
