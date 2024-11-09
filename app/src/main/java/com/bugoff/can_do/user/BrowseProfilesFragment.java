package com.bugoff.can_do.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bugoff.can_do.R;

import java.util.ArrayList;

/**
 * Fragment for browsing user profiles. This fragment displays a list of user profiles in a RecyclerView.
 * The profiles are fetched from a ViewModel that retrieves the data from a data source.
 */
public class BrowseProfilesFragment extends Fragment {
    /** RecyclerView for displaying user profiles */
    private RecyclerView recyclerViewProfiles;
    /** Adapter for the RecyclerView */
    private UserAdapter userAdapter;
    /** ProgressBar for indicating loading state */
    private ProgressBar progressBar;
    /** TextView for displaying an empty state message */
    private TextView emptyTextView;

    /** ViewModel for fetching user profiles */
    private BrowseProfilesViewModel browseProfilesViewModel;

    public BrowseProfilesFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse_profiles, container, false);

        recyclerViewProfiles = view.findViewById(R.id.recycler_view_profiles);
        progressBar = view.findViewById(R.id.progress_bar_profiles);
        emptyTextView = view.findViewById(R.id.text_view_empty_profiles);

        // Set up RecyclerView
        recyclerViewProfiles.setLayoutManager(new LinearLayoutManager(getContext()));
        userAdapter = new UserAdapter(new ArrayList<>());
        recyclerViewProfiles.setAdapter(userAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize ViewModel
        browseProfilesViewModel = new ViewModelProvider(this, new BrowseProfilesViewModelFactory(true))
                .get(BrowseProfilesViewModel.class);

        // Observe LiveData for profiles list
        browseProfilesViewModel.getProfilesList().observe(getViewLifecycleOwner(), users -> {
            if (users != null && !users.isEmpty()) {
                userAdapter.setUsers(users);
                recyclerViewProfiles.setVisibility(View.VISIBLE);
                emptyTextView.setVisibility(View.GONE);
            } else {
                recyclerViewProfiles.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.VISIBLE);
            }
            progressBar.setVisibility(View.GONE);
        });

        // Observe LiveData for error messages
        browseProfilesViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                progressBar.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.VISIBLE);
                emptyTextView.setText(errorMsg);
            }
        });
    }
}
