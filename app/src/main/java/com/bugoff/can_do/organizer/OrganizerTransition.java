package com.bugoff.can_do.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bugoff.can_do.R;
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.user.User;
import com.google.firebase.firestore.FirebaseFirestore;

public class OrganizerTransition extends AppCompatActivity {
    private FirebaseFirestore db;
    private String androidId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        checkUserFacility();
    }

    private void checkUserFacility() {
        db.collection("facilities").document(androidId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Facility exists, redirect to the next activity
                        navigateToNextActivity();
                    } else {
                        // Facility does not exist, prompt the user to create one
                        promptUserToCreateFacility();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to check facility data", Toast.LENGTH_SHORT).show()
                );
    }
    private void promptUserToCreateFacility() {
        setContentView(R.layout.activity_facility_edit);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        Button continueButton = findViewById(R.id.saveFacilityButton);
        continueButton.setOnClickListener(v -> saveFacilityAndProceed());
    }
    private void navigateToNextActivity() {
        Intent intent = new Intent(this, OrganizerMain.class); // Replace NewActivity with the target activity class
        startActivity(intent);
        finish();
    }

    private void saveFacilityAndProceed() {
        // Retrieve input fields
        EditText facilityNameInput = findViewById(R.id.facilityNameInput);
        EditText facilityAddressInput = findViewById(R.id.facilityAddressInput);

        String facilityName = facilityNameInput.getText().toString().trim();
        String facilityAddress = facilityAddressInput.getText().toString().trim();

        // Validate inputs
        if (facilityName.isEmpty() || facilityAddress.isEmpty()) {
            Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        User currentUser = GlobalRepository.getLoggedInUser();

        // Retrieve the existing Facility or create a new one
        Facility facility = currentUser.getFacility();
        if (facility == null) {
            facility = new Facility(currentUser);
            currentUser.setFacility(facility); // Ensure bidirectional reference
        }

        // Set the facility properties
        facility.setName(facilityName);
        facility.setAddress(facilityAddress);

        // Set an update listener to handle post-save actions
        facility.setOnUpdateListener(() -> {
            Toast.makeText(OrganizerTransition.this, "Facility saved successfully!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(OrganizerTransition.this, OrganizerMain.class));
            finish();
        });

        // Attach the snapshot listener
        facility.attachListener();

        // Save the facility to Firestore
        facility.setRemote();
    }
}
