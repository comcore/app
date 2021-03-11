package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.server.id.UserID;

public class GroupFragment extends Fragment {

    /** The id of the group that this fragment displays **/
    private int currentGroupID;

    public GroupFragment() {
        // Required empty public constructor
    }

    public static GroupFragment newInstance() {
        GroupFragment fragment = new GroupFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        /** TODO
         * The fragment should retrieve Group and User objects using the ids passed to it
         */

        /** When the fragment is created, GroupFragment should get a Group using the ID number
         * passed to it by MainFragment. **/
        currentGroupID = GroupFragmentArgs.fromBundle(getArguments()).getGroupID();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /** Displays the name of the current group
         * TODO
         * Currently displays the groupID, but should be able to access a group
         * object using currentGroupID, and then get the group name from that object**/
        TextView welcomeText = (TextView) view.findViewById(R.id.label_group_fragment);
        welcomeText.setText(Integer.toString(currentGroupID));

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.groupmenu, menu);

        /** TODO
         * If the current user is a moderator, display R.id.menu_group_moderator_actions
         * If the current user is an owner, display R.id.menu_group_owner_actions
         * and R.id.menu_group_moderator_actions
         */
        menu.setGroupVisible(R.id.menu_group_moderator_actions, false);
        menu.setGroupVisible(R.id.menu_group_owner_actions, false);
    }

    /**
     * Handles click events for the option menu
     * Most menu items are not visible unless viewing GroupFragment
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.view_members:
                /** Handle viewing list of members **/
                return true;
            case R.id.leave_group:
                /** Handle leaving group **/
                return true;
            case R.id.invite_member:
                /** Handle inviting a new member **/
                return true;
            case R.id.add_moderator:
                /** Handle adding moderator **/
                return true;
            case R.id.remove_moderator:
                /** Handle removing moderator **/
                return true;
            case R.id.kick_member:
                /** Handle kicking member **/
                return true;
            case R.id.dis_enable_chat:
                /** Handle disabling/enabling chat **/
                return true;
            case R.id.transfer_ownership:
                /** Handle transfer ownership **/
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}