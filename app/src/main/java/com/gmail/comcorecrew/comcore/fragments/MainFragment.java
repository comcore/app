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
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.caching.GroupStorage;
import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.dialogs.InviteLinkDialog;
import com.gmail.comcorecrew.comcore.dialogs.ViewEventsDialog;
import com.gmail.comcorecrew.comcore.dialogs.ViewGroupsDialog;
import com.gmail.comcorecrew.comcore.dialogs.ViewInvitesDialog;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.EventEntry;
import com.gmail.comcorecrew.comcore.server.entry.GroupInviteEntry;
import com.gmail.comcorecrew.comcore.server.id.CalendarID;
import com.gmail.comcorecrew.comcore.server.id.GroupID;

import java.util.ArrayList;
import java.util.Arrays;

public class MainFragment extends Fragment {
    private CustomAdapter groupAdapter;
    private LinearLayoutManager groupLayout;
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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Create the RecyclerView
        rvGroups = (RecyclerView) rootView.findViewById(R.id.main_recycler);
        groupLayout = new LinearLayoutManager(getActivity());
        rvGroups.setLayoutManager(groupLayout);
        groupAdapter = new CustomAdapter();
        rvGroups.setAdapter(groupAdapter);
        rvGroups.setItemAnimator(new DefaultItemAnimator());
        refresh();

        return rootView;
    }

    public void refresh() {
        GroupStorage.refresh(() -> {
            InviteLinkDialog.showIfPossible(this);
            groupAdapter.notifyDataSetChanged();
        });
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView welcomeText = (TextView) view.findViewById(R.id.label_main_fragment);
        welcomeText.setText("Welcome " + AppData.self.getName());
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
                /* Handle viewing list of members */

                new ViewInvitesDialog(this)
                        .show(getParentFragmentManager(), null);
                return true;
            case R.id.pin_group:
                new ViewGroupsDialog(this)
                        .show(getParentFragmentManager(), null);
                return true;
            case R.id.refresh_button:
                refresh();
                return true;
            case R.id.createGroupFragment:
                MainFragmentDirections.ActionMainFragmentToCreateGroupFragment action = MainFragmentDirections.actionMainFragmentToCreateGroupFragment(null);
                NavHostFragment.findNavController(MainFragment.this).navigate(action);
                return true;
            case R.id.viewSharedCalendar:
                NavHostFragment.findNavController(this).navigate(R.id.action_mainFragment_to_sharedCalendarFragment23);
                return true;
            case R.id.settingsFragment:
                /* Handle moving to the settings page */
                SettingsFragment.currentGroup = null;
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_mainFragment_to_settingsFragment);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Changes or removes the image on each group list item based on whether
     * the user is the owner, moderator, or neither. If the user is both owner and moderator,
     * the owner tag will take preference.
     *
     * The shape of the image tag can be changed in group_row_item.xml
     * The colors can be changed in colors.xml
     */
    public static void setRoleIndicator(ImageView tag, GroupRole role, boolean isDirect) {
        switch (role) {
            case OWNER:
                tag.setVisibility(View.VISIBLE);
                tag.setColorFilter(tag.getResources().getColor(R.color.owner_color));
                break;
            case MODERATOR:
                tag.setVisibility(View.VISIBLE);
                if (isDirect) {
                    tag.setColorFilter(tag.getResources().getColor(R.color.direct_color));
                } else {
                    tag.setColorFilter(tag.getResources().getColor(R.color.moderator_color));
                }
                break;
            case USER:
                tag.setVisibility(View.INVISIBLE);
                break;
        }
    }

    /** The CustomAdapter internal class sets up the RecyclerView, which displays
     * the list of groups in the GUI
     */
    class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TextView textView;
            private ImageView viewTag;
            private Group currentGroup;

            public ViewHolder(View view) {
                super(view);
                view.setOnClickListener(this);

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

                /* When a group box is clicked, pass its GroupId to the new group fragment instead
                  of passing the entire group.
                 */
                MainFragmentDirections.ActionMainFragmentToGroupFragment action = MainFragmentDirections.actionMainFragmentToGroupFragment(currentGroup.getGroupId());
                action.setGroupID(currentGroup.getGroupId());
                NavHostFragment.findNavController(MainFragment.this).navigate(action);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            // Create a new view, which defines the UI of the list item
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.group_row_item, viewGroup, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {

            Group group = AppData.getFromPos(position);
            viewHolder.getTextView().setText(group.getDisplayName());
            viewHolder.setGroup(group);

            setRoleIndicator(viewHolder.viewTag, group.getGroupRole(), group.isDirect());

            if (group.isPinned()) {
                viewHolder.textView.setTextColor(getResources().getColor(R.color.primary));
            }
            else {
                viewHolder.textView.setTextColor(getResources().getColor(R.color.black));
            }
        }

        @Override
        public int getItemCount() {
            return AppData.getGroupSize();
        }
    }
}