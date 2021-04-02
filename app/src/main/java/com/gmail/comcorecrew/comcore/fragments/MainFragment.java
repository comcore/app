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
import com.gmail.comcorecrew.comcore.caching.GroupStorage;
import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.dialogs.InviteLinkDialog;
import com.gmail.comcorecrew.comcore.dialogs.ViewGroupsDialog;
import com.gmail.comcorecrew.comcore.dialogs.ViewInvitesDialog;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.GroupInviteEntry;
import com.gmail.comcorecrew.comcore.server.id.GroupID;

import java.util.ArrayList;

public class MainFragment extends Fragment {
    private CustomAdapter groupAdapter;
    private LinearLayoutManager groupLayout;
    private ArrayList<Group> pinGroupList;
    private RecyclerView rvGroups;

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        pinGroupList = sortPinnedList();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Create the RecyclerView
        rvGroups = (RecyclerView) rootView.findViewById(R.id.main_recycler);
        groupLayout = new LinearLayoutManager(getActivity());
        rvGroups.setLayoutManager(groupLayout);
        groupAdapter = new CustomAdapter(pinGroupList, this);
        rvGroups.setAdapter(groupAdapter);
        rvGroups.setItemAnimator(new DefaultItemAnimator());
        refresh();

        return rootView;
    }

    public void refresh() {
        pinGroupList = sortPinnedList();
        InviteLinkDialog.showIfPossible(this);
        GroupStorage.refresh(groupAdapter::notifyDataSetChanged);
    }

    public ArrayList<Group> sortPinnedList() {
        ArrayList<Group> newList = AppData.groups;
        for (int i = 0; i < newList.size(); i++) {
            if (newList.get(i).isPinned()) {
                Group tempGroup = newList.get(i);
                newList.remove(i);
                newList.add(0, tempGroup);
            }
        }
        return newList;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView welcomeText = (TextView) view.findViewById(R.id.label_main_fragment);
        welcomeText.setText("Welcome " + ServerConnector.getUser().name);
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
            case R.id.pin_group:
                new ViewGroupsDialog()
                        .show(getParentFragmentManager(), null);
                return true;
            case R.id.refresh_button:
                refresh();
                return true;
            case R.id.createGroupFragment:
                NavHostFragment.findNavController(MainFragment.this)
                        .navigate(R.id.action_mainFragment_to_createGroupFragment);
                return true;
            case R.id.settingsFragment:
                /** Handle moving to the settings page. The GroupID is passed as NO_GROUP, which
                 * will not be treated like a real GroupID by SettingsFragment */
                MainFragmentDirections.ActionMainFragmentToSettingsFragment action = MainFragmentDirections.actionMainFragmentToSettingsFragment(new GroupID("NO_GROUP"));
                GroupID noGroup = new GroupID("NO_GROUP");
                action.setGroupId(noGroup);
                NavHostFragment.findNavController(MainFragment.this).navigate(action);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

/** The CustomAdapter internal class sets up the RecyclerView, which displays
 * the list of groups in the GUI
 */
 class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    private ArrayList<Group> sortedList;
    private MainFragment fragment;

    CustomAdapter(ArrayList<Group> sortedList, MainFragment fragment) {
        this.fragment = fragment;
        this.sortedList = sortedList;
    }

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

            /** When a group box is clicked, pass its GroupId to the new group fragment instead
             * of passing the entire group.
             */
            MainFragmentDirections.ActionMainFragmentToGroupFragment action = MainFragmentDirections.actionMainFragmentToGroupFragment(currentGroup.getGroupId());
            action.setGroupID(currentGroup.getGroupId());
            NavHostFragment.findNavController(fragment).navigate(action);
        }
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
        viewHolder.getTextView().setText(sortedList.get(position).getName());
        viewHolder.setGroup(sortedList.get(position));

        /* Changes or removes the image on each group list item based on whether
         * the user is the owner, moderator, or neither. If the user is both owner and moderator,
         * the owner tag will take preference.
         *
         * The shape of the image tag can be changed in group_row_item.xml
         * The colors can be changed in colors.xml
         */
        switch (sortedList.get(position).getGroupRole()) {
            case OWNER:
                viewHolder.viewTag.setVisibility(View.VISIBLE);
                viewHolder.viewTag.setColorFilter(fragment.getResources().getColor(R.color.owner_color));
                break;
            case MODERATOR:
                viewHolder.viewTag.setVisibility(View.VISIBLE);
                viewHolder.viewTag.setColorFilter(fragment.getResources().getColor(R.color.moderator_color));
                break;
            case USER:
                viewHolder.viewTag.setVisibility(View.INVISIBLE);
                break;
        }

        if (sortedList.get(position).isPinned()) {
            viewHolder.textView.setTextColor(fragment.getResources().getColor(R.color.primary_d2));
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return sortedList.size();
    }
}