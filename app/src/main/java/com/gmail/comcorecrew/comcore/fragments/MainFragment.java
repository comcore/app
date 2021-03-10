package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.server.id.UserID;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {

    // The ID of the user currently viewing the main page
    private static UserID currentUser;

    // An ArrayList of groups the current user is a member of
    private ArrayList<Group> UsersGroups;

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UsersGroups = new ArrayList<Group>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        // Create the RecyclerView
        RecyclerView rvGroups = (RecyclerView) rootView.findViewById(R.id.main_recycler);
        rvGroups.setLayoutManager(new LinearLayoutManager(getActivity()));
        CustomAdapter groupAdapter = new CustomAdapter(UsersGroups);
        rvGroups.setAdapter(groupAdapter);
        rvGroups.setItemAnimator(new DefaultItemAnimator());

        return rootView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.createGroupButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(MainFragment.this)
                        .navigate(R.id.action_mainFragment_to_createGroupFragment);
            }
        });

    }

    /** The CustomAdapter internal class sets up the RecyclerView, which displays
     * the list of groups in the GUI
     */
    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

        private ArrayList<Group> UserGroups;

        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder).
         */
        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView textView;

            public ViewHolder(View view) {
                super(view);
                // Define click listener for the ViewHolder's View

                textView = (TextView) view.findViewById(R.id.group_row_text);
            }

            public TextView getTextView() {
                return textView;
            }
        }

        /**
         * Initialize the dataset of the Adapter.
         */
        public CustomAdapter(ArrayList<Group> dataSet) {
            UserGroups = dataSet;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            // Create a new view, which defines the UI of the list item
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.group_row_item, viewGroup, false);

            return new ViewHolder(view);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {

            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            viewHolder.getTextView().setText(UserGroups.get(position).getName());
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return UserGroups.size();
        }
    }



}