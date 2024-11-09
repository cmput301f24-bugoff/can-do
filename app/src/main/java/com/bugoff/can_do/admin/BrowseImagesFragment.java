package com.bugoff.can_do.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bugoff.can_do.R;

/**
 * {@code BrowseImagesFragment} is unfinished
 */
public class BrowseImagesFragment extends Fragment {

    public BrowseImagesFragment() {
        // Required empty public constructor
    }

    public static BrowseImagesFragment newInstance() {
        return new BrowseImagesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_browse_images, container, false);
    }

    // TODO: Initialize RecyclerView and load images data
}
