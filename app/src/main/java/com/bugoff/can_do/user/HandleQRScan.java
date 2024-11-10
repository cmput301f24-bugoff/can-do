package com.bugoff.can_do.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.bugoff.can_do.R;
import com.bugoff.can_do.database.GlobalRepository;


/**
 * Handles the processing of QR codes scanned by the user.
 */
public class HandleQRScan {

    private static final String TAG = "HandleQRScan";

    /**
     * Processes the scanned QR code and navigates to the event details activity if the QR code is valid.
     *
     * @param qrCode  The scanned QR code.
     * @param context The context from which the QR code was scanned.
     */
    public static void processQRCode(String qrCode, Context context) {
        if (qrCode.startsWith("cando-")) {
            String eventId = qrCode.substring(6);  // Extract the ID part
            fetchEvent(eventId, context);
        } else {
            Log.e(TAG, "Invalid QR Code format");
        }
    }

    /**
     * Fetches the event details from Firestore using the event ID.
     *
     * @param eventId The ID of the event to fetch.
     * @param context The context from which the event details are fetched.
     */
    private static void fetchEvent(String eventId, Context context) {
        if (eventId == null || eventId.isEmpty()) {
            Log.e(TAG, "Event ID is null or empty. Cannot fetch event.");
            Toast.makeText(context, "Invalid Event ID", Toast.LENGTH_SHORT).show();
            return;
        }

        GlobalRepository.getEvent(eventId)
                .addOnSuccessListener(event -> {
                    if (event != null && event.getName() != null && event.getEventStartDate() != null) {
                        // Create a new instance of EventDetailsFragment and pass data through a Bundle
                        EventDetailsFragmentEntrant fragment = new EventDetailsFragmentEntrant();
                        Bundle args = new Bundle();
                        args.putString("event_name", event.getName());
                        args.putString("event_date", event.getEventStartDate().toString());
                        args.putString("selected_event_id", eventId); // Pass eventId explicitly for further use
                        fragment.setArguments(args);

                        // Replace the current fragment with EventDetailsFragment
                        FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, fragment); // Replace 'fragment_container' with your actual container ID
                        transaction.addToBackStack(null); // Optional: add to back stack for navigation
                        transaction.commit();
                    } else {
                        Log.e(TAG, "Event details are incomplete.");
                        Toast.makeText(context, "Event details are incomplete", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Event not found or failed to load: " + e.getMessage(), e);
                    Toast.makeText(context, "Event not found", Toast.LENGTH_SHORT).show();
                });

    }

}
