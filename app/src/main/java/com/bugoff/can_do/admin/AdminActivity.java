package com.bugoff.can_do.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bugoff.can_do.R;
import com.bugoff.can_do.event.EventsFragment;
import com.bugoff.can_do.user.BrowseProfilesFragment;

/**
 * {@code AdminActivity} serves as the main interface for admin users,
 * allowing them to browse events, profiles, and images. It manages
 * fragment transactions to display the corresponding sections based on
 * the admin's interactions.
 */
public class AdminActivity extends AppCompatActivity {

    /** Button to browse and manage events. */
    private Button browseEventsButton;

    /** Button to browse and manage user profiles. */
    private Button browseProfilesButton;

    /** Button to browse and manage images. */
    private Button browseImagesButton;

    /**
     * Called when the activity is first created. Initializes the layout,
     * binds the UI components, and sets up click listeners for admin actions.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains
     *                           the data it most recently supplied in {@link #onSaveInstanceState}.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize buttons
        browseEventsButton = findViewById(R.id.button_browse_events);
        browseProfilesButton = findViewById(R.id.button_browse_profiles);
        browseImagesButton = findViewById(R.id.button_browse_images);

        // Set click listeners
        browseEventsButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Handles the click event for browsing events. Loads the {@code EventsFragment}
             * with admin privileges.
             *
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {
                loadFragment(EventsFragment.newInstance(true)); // Pass isAdmin = true
            }
        });

        browseProfilesButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Handles the click event for browsing profiles. Loads the {@code BrowseProfilesFragment}.
             *
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {
                loadFragment(new BrowseProfilesFragment());
            }
        });

        browseImagesButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Handles the click event for browsing images. Loads the {@code BrowseImagesFragment}.
             *
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {
                loadFragment(new BrowseImagesFragment());
            }
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
}
