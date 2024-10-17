package com.bugoff.can_do;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    User currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect to Firestore
        new GlobalRepository();

        // Get Android ID
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        UserAuthenticator.authenticateUser(androidId)
                .addOnSuccessListener(user -> {
                    // Handle successful authentication
                    Log.d(TAG, "Authenticated User: " + user.getId());
                    currentUser = user;
                })
                .addOnFailureListener(e -> {
                    // Handle authentication failure
                    Log.e(TAG, "Authentication failed", e);
                });
    }

    public void databaseDemo(User user) {
        // Create a facility
        Facility facility = new Facility(user);
        facility.setRemote();
        facility.attachListener();
        // Create an event
        Event event = new Event(facility);
        event.setRemote();
        event.attachListener();
        Event event2 = new Event(facility);
        event2.setRemote();
        event2.attachListener();
    }
}
