package com.bugoff.can_do;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.database.UserAuthenticator;
import com.bugoff.can_do.notification.NotificationSettingsActivity;
import com.bugoff.can_do.user.QrCodeScannerFragment;
import com.bugoff.can_do.user.User;
import com.bugoff.can_do.user.UserViewModel;
import com.bugoff.can_do.user.UserViewModelFactory;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.Random;

/**
 * Main activity for the app. Handles navigation between different fragments using a BottomNavigationView.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private UserViewModel userViewModel;
    private GlobalRepository repository;
    private static final int PERMISSIONS_REQUEST_LOCATION = 1002;
    private ListenerRegistration notificationListener;

    /**
     * Initializes the activity and sets up the BottomNavigationView for navigation between different fragments.
     *
     * @param savedInstanceState The saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001);
            }
        }

        // Initialize repository - this can be either real or mock repository
        repository = new GlobalRepository();

        // Get Android ID
        @SuppressLint("HardwareIds") String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        UserAuthenticator.authenticateUser(androidId)
                .addOnSuccessListener(user -> {
                    if (user.getName() == null || user.getName().isEmpty()) {
                        setContentView(R.layout.activity_sign_in);
                        Log.d(TAG, "New User: " + user.getId());
                        initializeSignInScreen(user.getId());
                    } else {
                        // Existing user: proceed to load user data
                        setContentView(R.layout.activity_main);
                        Log.d(TAG, "Authenticated User: " + user.getId());

                        // Create UserViewModel using factory
                        UserViewModelFactory factory = new UserViewModelFactory(user.getId(), repository);
                        userViewModel = new ViewModelProvider(this, factory).get(UserViewModel.class);

                        GlobalRepository.setLoggedInUser(user);
                        setupNotificationListener(user.getId());

                        if (savedInstanceState == null) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, new HomeActivity())
                                    .commit();
                        }

                        setupBottomNavigation();
                        checkLocationPermissionAndFetchLocation(user);
                    }
                });
    }
    /**
     * Sets up a listener for notifications for the user.
     *
     * @param userId The ID of the user
     */
    private void setupNotificationListener(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query for notifications where the user is in pendingRecipients
        Query query = db.collection("notifications")
                .whereArrayContains("pendingRecipients", userId);

        notificationListener = query.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                Log.e(TAG, "Listen failed.", error);
                return;
            }

            if (snapshots != null && !snapshots.isEmpty()) {
                for (DocumentSnapshot doc : snapshots) {
                    String message = doc.getString("message");
                    String eventId = doc.getString("event");

                    // Fetch event name for the notification
                    if (eventId != null) {
                        GlobalRepository.getEvent(eventId).addOnSuccessListener(event -> {
                            if (event != null) {
                                String title = event.getName();
                                sendLocalNotification(title, message);
                            }
                        });
                    }

                    // Atomically remove this user from pendingRecipients
                    doc.getReference().update(
                            "pendingRecipients",
                            FieldValue.arrayRemove(userId)
                    );
                }
            }
        });
    }
    /**
     * Sends a local notification to the user.
     *
     * @param title   The title of the notification
     * @param message The message of the notification
     */
    private void sendLocalNotification(String title, String message) {
        // Check if notifications are enabled
        if (!NotificationSettingsActivity.areOrganizerNotificationsEnabled(this)) {
            return;
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "default",
                    "Default",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for app notifications");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.drawable.notifications_24px)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 250, 250, 250})
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);

        notificationManager.notify(new Random().nextInt(), builder.build());
    }
    /**
     * Checks if the app has location permission and fetches the location if permission is granted.
     *
     * @param user The user object
     */
    private void checkLocationPermissionAndFetchLocation(User user) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        } else {
            getLocationAndUpdateUser(user);
        }
    }
    /**
     * Requests location permission from the user and fetches the location if permission is granted.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                User user = GlobalRepository.getLoggedInUser();
                if (user != null) {
                    getLocationAndUpdateUser(user);
                }
            } else {
                // Permission denied
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    /**
     * Fetches the location of the user and updates the user object with the location.
     *
     * @param cuser The user object
     */
    private void getLocationAndUpdateUser(User cuser) {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            LocationRequest locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(1000) // Set interval for location updates
                    .setFastestInterval(500);

            fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null) {
                        Location location = locationResult.getLastLocation();
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            // Update user object
                            cuser.setLatitude(latitude);
                            cuser.setLongitude(longitude);
                            cuser.setRemote(); // Save to database

                            Log.d(TAG, "Location obtained: " + latitude + ", " + longitude);
                        }
                    }
                }
            }, Looper.getMainLooper());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets up the bottom navigation bar for switching between home, scan, and profile screens.
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment selectedFragment = null;

            if (id == R.id.nav_home) {
                Log.d(TAG, "Home clicked");
                selectedFragment = new HomeActivity();
            } else if (id == R.id.nav_scan) {
                Log.d(TAG, "Scan Activity clicked");
                selectedFragment = new QrCodeScannerFragment();
            } else if (id == R.id.nav_profile) {
                Log.d(TAG, "Profile clicked");
                selectedFragment = new UserProfileActivity();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }

    /**
     * Initializes the sign-in screen by setting up the UI elements and handling the submit button click.
     */
    private void initializeSignInScreen(String userId) {
        EditText nameEditText = findViewById(R.id.nameEditText);
        EditText emailEditText = findViewById(R.id.emailEditText);

        findViewById(R.id.submitButton).setOnClickListener(view -> {
            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (email.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            UserAuthenticator.authenticateUser(userId).addOnSuccessListener(user -> {
                user.setName(name);
                user.setEmail(email);
                Task<Void> addUserTask = repository.addUser(user);  // Use instance repository
                addUserTask
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(MainActivity.this, "Welcome " + name + "!", Toast.LENGTH_SHORT).show();
                            switchToHomeScreen();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to save user to database", e);
                            Toast.makeText(MainActivity.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                        });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "User authentication failed", e);
                Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
            });
        });
    }

    /**
     * Switches to the home screen activity.
     */
    private void switchToHomeScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public UserViewModel getUserViewModel() {
        return userViewModel;
    }

    // For testing purposes
    public void setRepository(GlobalRepository repository) {
        this.repository = repository;
    }

    @VisibleForTesting
    public GlobalRepository getRepository() {
        return repository;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationListener != null) {
            notificationListener.remove();
        }
    }
}