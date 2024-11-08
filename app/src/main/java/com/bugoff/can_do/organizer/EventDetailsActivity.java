package com.bugoff.can_do.organizer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bugoff.can_do.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class EventDetailsActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private TextView eventNameTextView;
    private TextView eventDateTextView;
    private ImageView qrCodeImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details_organizer);

        db = FirebaseFirestore.getInstance();
        eventNameTextView = findViewById(R.id.event_name_text_view);
        eventDateTextView = findViewById(R.id.event_date_text_view);
        qrCodeImageView = findViewById(R.id.idIVQrcode);

        String eventId = getIntent().getStringExtra("selected_event_id");

        if (eventId != null) {
            fetchEventDetails(eventId);
        }
    }

    private void fetchEventDetails(String eventId) {
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String eventName = documentSnapshot.getString("name");
                        Timestamp eventDate = documentSnapshot.getTimestamp("eventStartDate");
                        String qrCodeText = documentSnapshot.getString("qrCodeHash");

                        // Update your TextViews with the event details
                        eventNameTextView.setText(eventName);
                        eventDateTextView.setText(eventDate.toDate().toString());
                        generateQRCode(qrCodeText);
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load event details", Toast.LENGTH_SHORT).show();
                });
    }


    private void generateQRCode(String text)
    {
        BarcodeEncoder barcodeEncoder
                = new BarcodeEncoder();
        try {
            Bitmap bitmap = barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 400, 400);
            qrCodeImageView.setImageBitmap(bitmap); // Sets the Bitmap to ImageView
        }
        catch (WriterException e) {
            Log.e("TAG", e.toString());
        }
    }

}
