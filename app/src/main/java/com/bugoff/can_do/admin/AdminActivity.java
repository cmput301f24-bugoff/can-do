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

public class AdminActivity extends AppCompatActivity {

    private Button browseEventsButton;
    private Button browseProfilesButton;
    private Button browseImagesButton;

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
            @Override
            public void onClick(View view) {
                loadFragment(EventsFragment.newInstance(true)); // Pass isAdmin = true
            }
        });

        browseProfilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFragment(new BrowseProfilesFragment());
            }
        });

        browseImagesButton.setOnClickListener(new View.OnClickListener() {
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
