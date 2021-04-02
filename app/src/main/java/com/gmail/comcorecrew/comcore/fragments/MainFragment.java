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
import com.gmail.comcorecrew.comcore.dialogs.ViewInvitesDialog;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.GroupID;

public class MainFragment extends Fragment {
    private CustomAdapter groupAdapter;

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
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Create the RecyclerView
        RecyclerView rvGroups = (RecyclerView) rootView.findViewById(R.id.main_recycler);
        rvGroups.setLayoutManager(new LinearLayoutManager(getActivity()));
        groupAdapter = new CustomAdapter();
        rvGroups.setAdapter(groupAdapter);
        rvGroups.setItemAnimator(new DefaultItemAnimator());
        refresh();

        return rootView;
    }

    public void refresh() {
        InviteLinkDialog.showIfPossible(this);
        GroupStorage.refresh(groupAdapter::notifyDataSetChanged);
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

                /** When a group box is clicked, pass its GroupId to the new group fragment instead
                 * of passing the entire group.
                 */
                MainFragmentDirections.ActionMainFragmentToGroupFragment action = MainFragmentDirections.actionMainFragmentToGroupFragment(currentGroup.getGroupId());
                action.setGroupID(currentGroup.getGroupId());
                NavHostFragment.findNavController(MainFragment.this).navigate(action);
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
            viewHolder.getTextView().setText(AppData.groups.get(position).getName());
            viewHolder.setGroup(AppData.groups.get(position));

            /* Changes or removes the image on each group list item based on whether
             * the user is the owner, moderator, or neither. If the user is both owner and moderator,
             * the owner tag will take preference.
             *
             * The shape of the image tag can be changed in group_row_item.xml
             * The colors can be changed in colors.xml
             */
            switch (AppData.groups.get(position).getGroupRole()) {
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
            return AppData.groups.size();
        }
    }

}