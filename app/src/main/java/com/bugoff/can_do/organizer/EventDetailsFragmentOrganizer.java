package com.bugoff.can_do.organizer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.bugoff.can_do.R;
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.event.EventSelectedFragment;
import com.bugoff.can_do.event.EventViewModel;
import com.bugoff.can_do.event.EventViewModelFactory;
import com.bugoff.can_do.event.EventWaitlistFragment;
import com.bugoff.can_do.notification.SendNotificationFragment;
import com.bugoff.can_do.user.User;
import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventDetailsFragmentOrganizer extends Fragment {
    private FirebaseFirestore db;
    private TextView eventNameTextView;
    private TextView eventDateTextView;
    private TextView eventDescriptionTextView;
    private TextView eventLocationTextView;
    private ListView entrantsListView;
    private ImageView eventImageView;
    private String eventLocation;
    private String eventDescription;
    private String eventName;
    private String eventId;
    private ImageView qrCodeImageView;
    private static final String ARG_EVENT_ID = "selected_event_id";

    private static final String TAG = "EventDetailsFragmentOrg";

    public static EventDetailsFragmentOrganizer newInstance(String eventId) {
        EventDetailsFragmentOrganizer fragment = new EventDetailsFragmentOrganizer();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_details_organizer, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
        }

        // Bind views
        eventNameTextView = view.findViewById(R.id.class_tile);
        eventDateTextView = view.findViewById(R.id.class_date);
        eventDescriptionTextView = view.findViewById(R.id.class_description);
        eventLocationTextView = view.findViewById(R.id.class_location);
        eventImageView = view.findViewById(R.id.event_image);
        qrCodeImageView = view.findViewById(R.id.idIVQrcode);

        ImageButton backArrowButton = view.findViewById(R.id.back_arrow);
        ImageButton mapIconButton = view.findViewById(R.id.map_icon);
        ImageButton shareIconButton = view.findViewById(R.id.share_icon);
        ImageButton editGraphButton = view.findViewById(R.id.edit_graph);

        Button viewWatchListButton = view.findViewById(R.id.view_watch_list);
        Button viewSelectedButton = view.findViewById(R.id.view_selected_list);
        Button sendNotificationButton = view.findViewById(R.id.send_notification);

        View progressBar = view.findViewById(R.id.progress_bar);
        View mainContent =view.findViewById(R.id.main_content);

        // Set click listeners
        backArrowButton.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        mapIconButton.setOnClickListener(v -> openMapToLocation());
        shareIconButton.setOnClickListener(v -> shareEventDetails());

        EventViewModel viewModel = new ViewModelProvider(this, new EventViewModelFactory(eventId)).get(EventViewModel.class);

        editGraphButton.setOnClickListener(v -> Toast.makeText(requireContext(), "Edit Graph clicked", Toast.LENGTH_SHORT).show());
        viewWatchListButton.setOnClickListener(v -> showFragment(new EventWaitlistFragment(), "View Watch List clicked"));
        viewSelectedButton.setOnClickListener(v -> showFragment(new EventSelectedFragment(), "View Selected clicked"));

        sendNotificationButton.setOnClickListener(v -> {
            SendNotificationFragment fragment = new SendNotificationFragment();
            Bundle args = new Bundle();
            args.putString("eventId", eventId);
            fragment.setArguments(args);

            showFragment(fragment, "Send Notification clicked");
        });





        progressBar.setVisibility(View.VISIBLE);
        mainContent.setVisibility(View.INVISIBLE);

        fetchEventDetails(eventId);

        return view;
    }


    private void fetchEventDetails(String eventId) {

        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Fetch event details
                        eventName = documentSnapshot.getString("name");
                        eventDescription = documentSnapshot.getString("description");
                        Log.d(TAG, "fetchEventDetails: " + documentSnapshot.getString("facilityId"));
                        GlobalRepository.getFacility(documentSnapshot.getString("facilityId")).addOnSuccessListener(facility -> {
                            eventLocation = facility.getAddress();
                            Log.d(TAG, "fetchEventDetails: " + eventLocation);
                            eventLocationTextView.setText("Address: " + (eventLocation != null ? eventLocation : "N/A"));

                            checkLoadingComplete();

                        });
                        Timestamp eventDateTimestamp = documentSnapshot.getTimestamp("eventStartDate");
                        String imageUrl = documentSnapshot.getString("imageUrl"); // Get imageUrl
                        String qrCodeText = documentSnapshot.getString("qrCodeHash");
                        generateQRCode(qrCodeText);

                        // Update TextViews with event details
                        eventNameTextView.setText(eventName != null ? eventName : "N/A");
                        eventDescriptionTextView.setText(eventDescription != null ? eventDescription : "No Description");


                        if (eventDateTimestamp != null) {
                            Date eventDate = eventDateTimestamp.toDate();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                            String formattedDate = dateFormat.format(eventDate);
                            eventDateTextView.setText("Date: " + formattedDate);
                        } else {
                            eventDateTextView.setText("Date: N/A");
                        }

                        // Load the image using Glide
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .into(eventImageView);
                        }

                        checkLoadingComplete();


                    } else {
                        Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load event details", Toast.LENGTH_SHORT).show();
                });
    }


    private void checkLoadingComplete() {
        if (eventLocation != null && !eventNameTextView.getText().toString().equals("N/A")) {
            View progressBar = requireView().findViewById(R.id.progress_bar);
            View mainContent = requireView().findViewById(R.id.main_content);

            progressBar.setVisibility(View.GONE);
            mainContent.setVisibility(View.VISIBLE);
        }
    }


    private void openMapToLocation() {
        if (eventLocation != null && !eventLocation.isEmpty()) {
            Uri geoLocation = Uri.parse("geo:0,0?q=" + Uri.encode(eventLocation));
            Intent intent = new Intent(Intent.ACTION_VIEW, geoLocation);
            intent.setPackage("com.google.android.apps.maps");
            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "No map application found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "Event location not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareEventDetails() {
        String shareContent = "Check out this event: " + eventName + "\n"
                + "Date: " + eventDateTextView.getText().toString().replace("Date: ", "") + "\n"
                + "Location: " + eventLocation + "\n"
                + "Description: " + eventDescription;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
        startActivity(Intent.createChooser(shareIntent, "Share Event via"));
    }

    private void generateQRCode(String text) {
        BarcodeEncoder barcodeEncoder
                = new BarcodeEncoder();
        try {
            Bitmap bitmap = barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 400, 400);
            qrCodeImageView.setImageBitmap(bitmap); // Sets the Bitmap to ImageView
        } catch (WriterException e) {
            Log.e("TAG", e.toString());
        }
    }

    private void showFragment(Fragment fragment, String logMessage) {
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);

        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        Log.d("EventDetailsFragmentOrganizer", logMessage);
    }
}
