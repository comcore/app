package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.abstracts.CustomModule;

public class CustomFragment extends Fragment {
    public static CustomModule custom;
    private View customView;

    public CustomFragment () {
        // Required empty public constructor
    }

    public static CustomFragment newInstance() {
        return new CustomFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Refresh custom data and inflate view
        custom.fromCache();
        custom.refresh();
        return inflater.inflate(custom.getLayout(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        custom.onViewCreated(view, this);
        customView = view;
        custom.setCallback(this::refresh);
    }

    public void refresh() {
        custom.refreshView();
    }
}