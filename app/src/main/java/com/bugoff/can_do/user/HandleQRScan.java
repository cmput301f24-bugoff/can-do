package com.bugoff.can_do.user;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.organizer.EventDetailsActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentSnapshot;

public class HandleQRScan {

    private static final String TAG = "HandleQRScan";

    public static void processQRCode(String qrCode, Context context) {
        if (qrCode.startsWith("cando-")) {
            String eventId = qrCode.substring(6);  // Extract the ID part
            fetchEvent(eventId, context);
        } else {
            Log.e(TAG, "Invalid QR Code format");
        }
    }

    private static void fetchEvent(String eventId, Context context) {
        if (eventId == null || eventId.isEmpty()) {
            Log.e(TAG, "Event ID is null or empty. Cannot fetch event.");
            Toast.makeText(context, "Invalid Event ID", Toast.LENGTH_SHORT).show();
            return;
        }

        GlobalRepository.getEvent(eventId)
                .addOnSuccessListener(event -> {
                    // Show Event Activity with fetched data
                    Intent intent = new Intent(context, EventDetailsActivity.class);
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
