package com.bugoff.can_do;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.database.UserAuthenticator;

public class signInActivity extends AppCompatActivity {
    private static final String TAG = "SignInActivity";
    private EditText nameEditText;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        nameEditText = findViewById(R.id.nameEditText);
        submitButton = findViewById(R.id.submitButton);

        submitButton.setOnClickListener(view -> {
            String name = nameEditText.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(signInActivity.this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the Android ID and authenticate
            String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

            // Authenticate and set the name
            UserAuthenticator.authenticateUser(androidId)
                    .addOnSuccessListener(user -> {
                        user.setName(name);
                        GlobalRepository.addUser(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(signInActivity.this, "Welcome " + name + "!", Toast.LENGTH_SHORT).show();
                                    navigateToMainActivity();
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to save user to database", e));
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "User authentication failed", e));
        });
    }

    // Helper to navigate to MainActivity
    private void navigateToMainActivity() {
        Intent intent = new Intent(signInActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

