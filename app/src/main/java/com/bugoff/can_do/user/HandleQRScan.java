package com.bugoff.can_do.user;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.organizer.EventDetailsActivityOrganizer;

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
                    // Show Event Activity with fetched data
                    Intent intent = new Intent(context, EventDetailsActivityEntrant.class);
                    intent.putExtra("event_name", event.getName());
                    intent.putExtra("event_date", event.getEventStartDate().toString());
                    intent.putExtra("selected_event_id", eventId); // Pass eventId explicitly for further use
                    context.startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Event not found: " + e.getMessage());
                    Toast.makeText(context, "Event not found", Toast.LENGTH_SHORT).show();
                });
    }

}
