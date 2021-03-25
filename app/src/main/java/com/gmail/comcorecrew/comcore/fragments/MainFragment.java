package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.dialogs.ViewInvitesDialog;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.info.GroupInfo;

import java.util.ArrayList;
import java.util.Arrays;

public class MainFragment extends Fragment {
    public static ArrayList<Group> groups =  new ArrayList<>();

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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Create the RecyclerView
        RecyclerView rvGroups = (RecyclerView) rootView.findViewById(R.id.main_recycler);
        rvGroups.setLayoutManager(new LinearLayoutManager(getActivity()));
        groupAdapter = new CustomAdapter();
        rvGroups.setAdapter(groupAdapter);
        rvGroups.setItemAnimator(new DefaultItemAnimator());

        return rootView;
    }

    public void refresh() {
        groupAdapter.refresh();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView welcomeText = (TextView) view.findViewById(R.id.label_main_fragment);
        welcomeText.setText("Welcome " + ServerConnector.getUser().name);

        view.findViewById(R.id.createGroupButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(MainFragment.this)
                        .navigate(R.id.action_mainFragment_to_createGroupFragment);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.mainmenu, menu);
    }

    /**
     * Handles click events for the option menu
     * Most menu items are not visible unless viewing GroupFragment
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.invitesFragment:
                /** Handle viewing list of members **/

                new ViewInvitesDialog(this)
                        .show(getParentFragmentManager(), null);
                return true;
            case R.id.refresh_button:
                refresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** The CustomAdapter internal class sets up the RecyclerView, which displays
     * the list of groups in the GUI
     */
    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
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
        public CustomAdapter() {
            refresh();
        }

        private void refresh() {
            ServerConnector.getGroups(result -> {
                if (result.isFailure()) {
                    new ErrorDialog(R.string.error_cannot_connect)
                            .show(getParentFragmentManager(), null);
                    return;
                }

                ServerConnector.getGroupInfo(Arrays.asList(result.data), 0, result1 -> {
                    if (result1.isFailure()) {
                        new ErrorDialog(R.string.error_cannot_connect)
                                .show(getParentFragmentManager(), null);
                        return;
                    }

                    GroupInfo[] info = result1.data;
                    ArrayList<Group> userGroups = new ArrayList<>();
                    for (int i = 0; i < result.data.length; i++) {
                        Group nextGroup = new Group(info[i].name,  info[i].id,
                                info[i].role, info[i].muted);
                        userGroups.add(nextGroup);
                    }
                    groups = userGroups;

                    notifyDataSetChanged();
                });
            });
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
            viewHolder.getTextView().setText(groups.get(position).getName());
            viewHolder.setGroup(groups.get(position));

            /* Changes or removes the image on each group list item based on whether
             * the user is the owner, moderator, or neither. If the user is both owner and moderator,
             * the owner tag will take preference.
             *
             * The shape of the image tag can be changed in group_row_item.xml
             * The colors can be changed in colors.xml
             */
            switch (groups.get(position).getGroupRole()) {
                case OWNER:
                    viewHolder.viewTag.setVisibility(View.VISIBLE);
                    viewHolder.viewTag.setColorFilter(getResources().getColor(R.color.owner_color));
                    break;
                case MODERATOR:
                    viewHolder.viewTag.setVisibility(View.VISIBLE);
                    viewHolder.viewTag.setColorFilter(getResources().getColor(R.color.moderator_color));
                    break;
                case USER:
                    viewHolder.viewTag.setVisibility(View.INVISIBLE);
                    break;
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return groups.size();
        }
    }

}