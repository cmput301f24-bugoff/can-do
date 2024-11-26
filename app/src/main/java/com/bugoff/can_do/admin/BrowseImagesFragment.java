package com.bugoff.can_do.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bugoff.can_do.R;
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.event.Event;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BrowseImagesFragment extends Fragment {
    private static final String TAG = "BrowseImagesFragment";
    private RecyclerView recyclerView;
    private ImageAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyView;
    private List<Event> eventsWithImages = new ArrayList<>();

    public BrowseImagesFragment() {
        // Required empty public constructor
    }

    public static BrowseImagesFragment newInstance() {
        return new BrowseImagesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse_images, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_images);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyView = view.findViewById(R.id.empty_view);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new ImageAdapter(eventsWithImages, this::handleImageDelete);
        recyclerView.setAdapter(adapter);

        loadImages();

        return view;
    }

    private void loadImages() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);

        GlobalRepository.getEventsCollection()
                .whereNotEqualTo("imageUrl", null)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventsWithImages.clear();
                    int totalDocuments = queryDocumentSnapshots.size();
                    if (totalDocuments == 0) {
                        updateViewVisibility();
                        return;
                    }

                    AtomicInteger processedDocuments = new AtomicInteger(0);

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String facilityId = document.getString("facilityId");
                        if (facilityId != null) {
                            GlobalRepository.getFacility(facilityId)
                                    .addOnSuccessListener(facility -> {
                                        Event event = new Event(facility, document);
                                        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                                            eventsWithImages.add(event);
                                            adapter.updateEvents(eventsWithImages);
                                        }

                                        // Check if all documents have been processed
                                        if (processedDocuments.incrementAndGet() == totalDocuments) {
                                            updateViewVisibility();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("BrowseImagesFragment", "Error loading facility: " + e.getMessage());
                                        // Still increment counter even on failure
                                        if (processedDocuments.incrementAndGet() == totalDocuments) {
                                            updateViewVisibility();
                                        }
                                    });
                        } else {
                            // Handle case where facilityId is null
                            if (processedDocuments.incrementAndGet() == totalDocuments) {
                                updateViewVisibility();
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("BrowseImagesFragment", "Error loading events: " + e.getMessage());
                    progressBar.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                    emptyView.setText("Error loading images: " + e.getMessage());
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to load images: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleImageDelete(Event event) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to remove this image from the event?")
                .setPositiveButton("Delete", (dialog, which) -> deleteImage(event))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteImage(Event event) {
        progressBar.setVisibility(View.VISIBLE);

        // Update the event in Firestore to remove the image URL
        GlobalRepository.getEventsCollection().document(event.getId())
                .update("imageUrl", null)
                .addOnSuccessListener(aVoid -> {
                    // Remove from local list and update adapter
                    eventsWithImages.remove(event);
                    adapter.updateEvents(eventsWithImages);
                    updateViewVisibility();

                    // Show success message
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Image deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to delete image: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateViewVisibility() {
        progressBar.setVisibility(View.GONE);
        if (eventsWithImages.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText("No images found");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}