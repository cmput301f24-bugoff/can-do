package com.bugoff.can_do.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bugoff.can_do.R;
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.facility.FacilityViewModel;
import com.bugoff.can_do.facility.FacilityViewModelFactory;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Activity for editing a facility. Allows the user to view, create, or update facility details such as
 * name and address. Uses FacilityViewModel to handle data operations.
 */
public class FacilityEdit extends AppCompatActivity {
    private TextInputEditText facilityNameInput, facilityAddressInput;
    private Button saveFacilityButton;
    private FacilityViewModel facilityViewModel;
    private GlobalRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facility_edit);

        // Initialize repository
        repository = new GlobalRepository();

        facilityNameInput = findViewById(R.id.facilityNameInput);
        facilityAddressInput = findViewById(R.id.facilityAddressInput);
        saveFacilityButton = findViewById(R.id.saveFacilityButton);

        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Initialize ViewModel using the Factory
        FacilityViewModelFactory factory = new FacilityViewModelFactory(androidId, repository);
        facilityViewModel = new ViewModelProvider(this, factory).get(FacilityViewModel.class);

        // Set up the back button
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Observe facility data
        facilityViewModel.getName().observe(this, name -> {
            if (name != null) {
                facilityNameInput.setText(name);
            }
        });

        facilityViewModel.getAddress().observe(this, address -> {
            if (address != null) {
                facilityAddressInput.setText(address);
            }
        });

        facilityViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(FacilityEdit.this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // Save button functionality
        saveFacilityButton.setOnClickListener(v -> {
            if (areFieldsValid()) {
                saveFacilityData();
            } else {
                Toast.makeText(FacilityEdit.this, "Please fill out both fields", Toast.LENGTH_SHORT).show();
            }
        });
    }
    /**
     * Checks if the facility name and address fields are valid.
     *
     * @return True if both fields are valid, false otherwise.
     */
    private boolean areFieldsValid() {
        return facilityNameInput.getText() != null && !facilityNameInput.getText().toString().isEmpty()
                && facilityAddressInput.getText() != null && !facilityAddressInput.getText().toString().isEmpty();
    }

    /**
     * Saves the facility data to Firestore and navigates back to the OrganizerMain activity.
     */
    private void saveFacilityData() {
        String name = facilityNameInput.getText().toString();
        String address = facilityAddressInput.getText().toString();

        facilityViewModel.setName(name);
        facilityViewModel.setAddress(address);

        Toast.makeText(FacilityEdit.this, "Facility saved successfully", Toast.LENGTH_SHORT).show();

        // Navigate to OrganizerMain
        Intent intent = new Intent(this, OrganizerMain.class);
        startActivity(intent);
        finish();
    }

    // For testing purposes
    public void setRepository(GlobalRepository repository) {
        this.repository = repository;
        // Reinitialize ViewModel with new repository
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        FacilityViewModelFactory factory = new FacilityViewModelFactory(androidId, repository);
        facilityViewModel = new ViewModelProvider(this, factory).get(FacilityViewModel.class);
    }
}
