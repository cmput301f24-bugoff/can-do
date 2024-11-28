package com.bugoff.can_do.organizer;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bugoff.can_do.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class WaitingListMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap googleMap;
    private String eventId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_list_map);

        eventId = getIntent().getStringExtra("EVENT_ID");
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        fetchWaitingListAndPlotMarkers();
    }

    private void fetchWaitingListAndPlotMarkers() {
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Fetch the waitingListEntrants array
                        List<String> waitingListEntrants = (List<String>) documentSnapshot.get("waitingListEntrants");
                        if (waitingListEntrants != null && !waitingListEntrants.isEmpty()) {
                            for (String userId : waitingListEntrants) {
                                Log.d("WaitingListMap", "Fetched userId: " + userId);

                                // Fetch user data based on userId
                                db.collection("users").document(userId)
                                        .get()
                                        .addOnSuccessListener(userSnapshot -> {
                                            if (userSnapshot.exists()) {
                                                String name = userSnapshot.getString("name");
                                                Double latitude = userSnapshot.getDouble("latitude");
                                                Double longitude = userSnapshot.getDouble("longitude");

                                                Log.d("WaitingListMap", "User data: name=" + name + ", Latitude=" + latitude + ", Longitude=" + longitude);

                                                // Add marker if latitude and longitude are available
                                                if (latitude != null && longitude != null) {
                                                    LatLng userLocation = new LatLng(latitude, longitude);
                                                    googleMap.addMarker(new MarkerOptions()
                                                            .position(userLocation)
                                                            .title(name));
                                                } else {
                                                    Log.e("WaitingListMap", "Missing latitude or longitude for user: " + name);
                                                }
                                            } else {
                                                Log.e("WaitingListMap", "User document does not exist for userId: " + userId);
                                            }
                                        })
                                        .addOnFailureListener(e -> Log.e("WaitingListMap", "Failed to fetch user document", e));
                            }
                        } else {
                            Log.d("WaitingListMap", "No users in the waiting list.");
                        }
                    } else {
                        Log.e("WaitingListMap", "Event document does not exist for eventId: " + eventId);
                    }
                })
                .addOnFailureListener(e -> Log.e("WaitingListMap", "Failed to fetch event document", e));
    }



    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
