package com.bugoff.can_do.organizer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bugoff.can_do.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class EventsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_events, container, false);

//        ListView eventListView = view.findViewById(R.id.event_list_view);
        FloatingActionButton fabAddEvent = view.findViewById(R.id.fab_add_event);

        // Set up your ListView and FAB listeners here
        fabAddEvent.setOnClickListener(v -> {
            // Code to add a new event (e.g., open a new activity or dialog)
        });

        return view;
    }
}
