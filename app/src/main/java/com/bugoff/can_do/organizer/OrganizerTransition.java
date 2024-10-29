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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

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
        db.collection("users").document(androidId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("facility") && documentSnapshot.contains("address")) {
                        // Facility and address exist, redirect to another activity
                        navigateToNextActivity();
                    } else {
                        // Facility and address do not exist, prompt the user to create them
                        promptUserToCreateFacility();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to check facility data", Toast.LENGTH_SHORT).show());
    }
    private void promptUserToCreateFacility() {
        setContentView(R.layout.activity_facility_edit);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        Button continueButton = findViewById(R.id.saveFacilityButton);
        continueButton.setOnClickListener(v -> saveFacilityAndProceed());
    }
    private void navigateToNextActivity() {
        Intent intent = new Intent(this, OrganizerHome.class); // Replace NewActivity with the target activity class
        startActivity(intent);
        finish();
    }

    private void saveFacilityAndProceed() {
        EditText facilityNameInput = findViewById(R.id.facilityNameInput);
        EditText facilityAddressInput = findViewById(R.id.facilityAddressInput);

        String facilityName = facilityNameInput.getText().toString();
        String facilityAddress = facilityAddressInput.getText().toString();

        if (facilityName.isEmpty() || facilityAddress.isEmpty()) {
            Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a map with the new facility data
        Map<String, Object> facilityData = new HashMap<>();
        facilityData.put("facility", facilityName);
        facilityData.put("address", facilityAddress);

        // Update only the facility and address fields in the document
        db.collection("users").document(androidId)
                .update(facilityData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(OrganizerTransition.this, "Facility saved successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(OrganizerTransition.this, OrganizerHome.class)); // Replace with your target activity
                    finish();
                })
                .addOnFailureListener(e -> {
                    // If update fails, check if document exists, and create it if necessary
                    db.collection("users").document(androidId)
                            .set(facilityData, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(OrganizerTransition.this, "Facility created successfully!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(OrganizerTransition.this, OrganizerHome.class)); // Replace with your target activity
                                finish();
                            })
                            .addOnFailureListener(error -> Toast.makeText(OrganizerTransition.this, "Failed to save facility", Toast.LENGTH_SHORT).show());
                });
    }

}
