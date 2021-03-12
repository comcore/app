package com.gmail.comcorecrew.comcore.fragments;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.UserEntry;
import com.gmail.comcorecrew.comcore.server.id.UserID;

import java.util.ArrayList;
import java.util.UUID;

public class MainFragment extends Fragment {

    private UserEntry currentUser;
    // An ArrayList of groups the current user is a member of
    private ArrayList<Group> UsersGroups;

    private CustomAdapter groupAdapter;

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
        setHasOptionsMenu(true);
        UsersGroups = new ArrayList<Group>();
        currentUser = ServerConnector.getUser();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ServerConnector.getGroups( result -> {

            if (result.isSuccess()) {
                for (int i = 0; i < result.data.length; i++) {
                    Group nextGroup = new Group(this.getContext(), result.data[i].name,
                            result.data[i].id, result.data[i].role, result.data[i].muted);
                    UsersGroups.add(nextGroup);
                }

                // Create the RecyclerView
                RecyclerView rvGroups = (RecyclerView) rootView.findViewById(R.id.main_recycler);
                rvGroups.setLayoutManager(new LinearLayoutManager(getActivity()));
                groupAdapter = new CustomAdapter(currentUser, UsersGroups);
                rvGroups.setAdapter(groupAdapter);
                rvGroups.setItemAnimator(new DefaultItemAnimator());

                return;
            }
            else if (result.isFailure()) {
                new ErrorDialog(R.string.error_unknown)
                        .show(getParentFragmentManager(), null);
                return;
            }
        });

        return rootView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /** Creates a welcome message based on the user's id
         * This currently prints the user's id, but later should print their name **/
        TextView welcomeText = (TextView) view.findViewById(R.id.label_main_fragment);
        welcomeText.setText("Welcome " + currentUser.toString());

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
        private UserID user;

        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder).
         */
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TextView textView;
            private ImageView viewTag;
            private Group currentGroup;

            public ViewHolder(View view) {
                super(view);
                view.setOnClickListener(this);
                // Define click listener for the ViewHolder's View

                textView = (TextView) view.findViewById(R.id.group_row_text);
                viewTag = (ImageView) view.findViewById(R.id.group_row_tag);

            }

            public TextView getTextView() {
                return textView;
            }

            public void setGroup(Group currentGroup) {
                this.currentGroup = currentGroup;
            }

            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();
                bundle.putParcelable("currentGroup", currentGroup);

                NavHostFragment.findNavController(MainFragment.this).navigate(R.id.action_mainFragment_to_groupFragment, bundle);
            }
        }

        /**
         * Initialize the dataset of the Adapter.
         */
        public CustomAdapter(UserEntry user, ArrayList<Group> dataSet) {
            this.user = user.id;
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
            viewHolder.setGroup(UserGroups.get(position));

            /** Changes or removes the image on each group list item based on whether
             * the user is the owner, moderator, or neither. If the user is both owner and moderator,
             * the owner tag will take preference.
             *
             * The shape of the image tag can be changed in group_row_item.xml
             * The colors can be changed in colors.xml
             */
            if (UserGroups.get(position).getGroupRole() == GroupRole.OWNER) {
                viewHolder.viewTag.setColorFilter(getResources().getColor(R.color.owner_color));
            }
            else if (UserGroups.get(position).getGroupRole() == GroupRole.MODERATOR) {
                viewHolder.viewTag.setColorFilter(getResources().getColor(R.color.moderator_color));
            }
            else {
                viewHolder.viewTag.setVisibility(View.INVISIBLE);
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return UserGroups.size();
        }
    }

}