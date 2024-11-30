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
import com.bugoff.can_do.database.FirestoreHelper;
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.user.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BrowseImagesFragment extends Fragment implements ImageAdapter.OnDeleteClickListener {
    private static final String TAG = "BrowseImagesFragment";
    private RecyclerView recyclerView;
    private ImageAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse_images, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_images);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyView = view.findViewById(R.id.empty_view);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new ImageAdapter(this);
        recyclerView.setAdapter(adapter);

        loadImages();

        return view;
    }

    private void loadImages() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);

        List<Task<?>> tasks = new ArrayList<>();
        tasks.add(loadEventImages());
        tasks.add(loadUserImages());

        Tasks.whenAllComplete(tasks)
                .addOnSuccessListener(taskSnapshots -> updateViewVisibility())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading images", e);
                    showError("Error loading images");
                });
    }

    private Task<?> loadEventImages() {
        return GlobalRepository.getEventsCollection()
                .whereNotEqualTo("base64Image", null)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        return;
                    }

                    // Count how many events we need to process
                    int totalEvents = queryDocumentSnapshots.size();
                    final int[] processedEvents = {0};

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String facilityId = document.getString("facilityId");
                        if (facilityId != null) {
                            GlobalRepository.getFacility(facilityId)
                                    .addOnSuccessListener(facility -> {
                                        Event event = new Event(facility, document);
                                        String base64Image = event.getBase64Image();
                                        if (base64Image != null && !base64Image.isEmpty()) {
                                            // Add individual event to avoid overwriting
                                            List<Event> singleEvent = new ArrayList<>();
                                            singleEvent.add(event);
                                            adapter.addItems(singleEvent);
                                        }
                                        processedEvents[0]++;

                                        // Update visibility once all events are processed
                                        if (processedEvents[0] == totalEvents) {
                                            updateViewVisibility();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error loading facility: " + e);
                                        processedEvents[0]++;
                                        if (processedEvents[0] == totalEvents) {
                                            updateViewVisibility();
                                        }
                                    });
                        } else {
                            processedEvents[0]++;
                            if (processedEvents[0] == totalEvents) {
                                updateViewVisibility();
                            }
                        }
                    }
                });
    }

    private Task<?> loadUserImages() {
        return GlobalRepository.getUsersCollection()
                .whereNotEqualTo("base64Image", null)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = new User(document);
                        String base64Image = user.getBase64Image();
                        if (base64Image != null && !base64Image.isEmpty()) {
                            users.add(user);
                        }
                    }
                    if (!users.isEmpty()) {
                        adapter.addItems(users);
                    }
                    updateViewVisibility();
                });
    }

    @Override
    public void onDeleteClick(Object item, boolean isEvent) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to remove this image?")
                .setPositiveButton("Delete", (dialog, which) -> deleteImage(item, isEvent))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteImage(Object item, boolean isEvent) {
        progressBar.setVisibility(View.VISIBLE);

        String collectionPath = isEvent ? "events" : "users";
        String itemId = isEvent ? ((Event) item).getId() : ((User) item).getId();

        FirestoreHelper.getInstance().getDb().collection(collectionPath)
                .document(itemId)
                .update("base64Image", null)
                .addOnSuccessListener(aVoid -> {
                    adapter.updateItems(new ArrayList<>()); // Clear the list
                    loadImages(); // Reload all images
                    showSuccess("Image deleted successfully");
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    showError("Failed to delete image");
                });
    }

    private void updateViewVisibility() {
        progressBar.setVisibility(View.GONE);
        if (adapter.getItemCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText("No images found");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
        Log.e(TAG, message);
    }

    private void showSuccess(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}