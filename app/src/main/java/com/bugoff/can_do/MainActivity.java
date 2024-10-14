package com.bugoff.can_do;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect to Firestore
        GlobalRepository globalRepository = new GlobalRepository();

        // Get Android ID
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        UserAuthenticator.authenticateUser(globalRepository, androidId)
                .addOnSuccessListener(user -> {
                    // Handle successful authentication
                    Log.d("MainActivity", "Authenticated User: " + user.getAndroidId());
                    Facility facility = new Facility(user);
                })
                .addOnFailureListener(e -> {
                    // Handle authentication failure
                    Log.e("MainActivity", "Authentication failed", e);
                });
    }
}
