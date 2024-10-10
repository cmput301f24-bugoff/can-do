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
        UserRepository userRepository = new UserRepository();

        // Get Android ID
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Get user from Firestore
        userRepository.getUser(androidId,
                user -> {
                    // User found
                    Log.d("MainActivity", "User found: " + user.getAndroidId() + " " + user.getName());
                },
                e -> {
                    // User not found
                    // Add user to Firestore
                    User user = new User(androidId, "QWERTY XYZ");
                    userRepository.addUser(user,
                            aVoid -> {
                                // User added
                                Log.d("MainActivity", "User added: " + user.getAndroidId() + " " + user.getName());
                            },
                            e1 -> {
                                // User not added
                                Log.e("MainActivity", "Error adding user", e1);
                            });
                });
    }
}
