package com.bugoff.can_do.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bugoff.can_do.R;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.user.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

public class FacilityEdit extends AppCompatActivity {
    private TextInputEditText facilityNameInput, facilityAddressInput;
    private Button saveFacilityButton;
    private Facility facility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facility_edit);

        facilityNameInput = findViewById(R.id.facilityNameInput);
        facilityAddressInput = findViewById(R.id.facilityAddressInput);
        saveFacilityButton = findViewById(R.id.saveFacilityButton);

        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Initialize or load facility
        loadFacilityData(db, androidId);

        // Set up the back button
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Save button functionality
        saveFacilityButton.setOnClickListener(v -> {
            if (areFieldsValid()) {
                saveFacilityData();
            } else {
                Toast.makeText(FacilityEdit.this, "Please fill out both fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFacilityData(FirebaseFirestore db, String androidId) {
        db.collection("facilities").document(androidId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        facility = new Facility(documentSnapshot);
                        facilityNameInput.setText(facility.getName());
                        facilityAddressInput.setText(facility.getAddress());
                    } else {
                        // Facility does not exist; create a new instance
                        User owner = new User(androidId); // Ensure User class has a constructor accepting ID
                        facility = new Facility(owner);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(FacilityEdit.this, "Failed to load facility data", Toast.LENGTH_SHORT).show());
    }

    private boolean areFieldsValid() {
        return facilityNameInput.getText() != null && !facilityNameInput.getText().toString().isEmpty()
                && facilityAddressInput.getText() != null && !facilityAddressInput.getText().toString().isEmpty();
    }

    private void saveFacilityData() {
        String name = facilityNameInput.getText().toString();
        String address = facilityAddressInput.getText().toString();

        facility.setName(name);
        facility.setAddress(address);

        facility.setRemote(); // Save to Firestore using Facility's method
        Toast.makeText(FacilityEdit.this, "Facility saved successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}
