package com.bugoff.can_do.organizer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bugoff.can_do.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class EventsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_events, container, false);

        // ListView eventListView = view.findViewById(R.id.event_list_view);
        FloatingActionButton fabAddEvent = view.findViewById(R.id.fab_add_event);

        // Set up FAB listener to open CreateEventFragment
        fabAddEvent.setOnClickListener(v -> {
            Fragment createEventFragment = new CreateEventFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container_organizer, createEventFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }
}
