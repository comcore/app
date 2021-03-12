package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
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
import com.gmail.comcorecrew.comcore.classes.User;
import com.gmail.comcorecrew.comcore.dialogs.AddMemberDialog;
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.dialogs.StringErrorDialog;
import com.gmail.comcorecrew.comcore.dialogs.ViewMembersDialog;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.gmail.comcorecrew.comcore.server.id.UserID;

import java.util.ArrayList;

public class GroupFragment extends Fragment {
    private MainFragment fragment;
    private Group currentGroup;

    public GroupFragment() {
        // Required empty public constructor
    }

    public static GroupFragment newInstance() {
        GroupFragment fragment = new GroupFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            currentGroup = bundle.getParcelable("currentGroup");

        }
        else {
            new ErrorDialog(R.string.error_unknown)
                    .show(getParentFragmentManager(), null);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /** Displays the name of the current group */
        TextView welcomeText = (TextView) view.findViewById(R.id.label_group_fragment);
        welcomeText.setText(currentGroup.getName());

        /**
         * If the "back" button is clicked, return to the main page
         */
        view.findViewById(R.id.group_back_button).setOnClickListener(clickedView -> {
            NavHostFragment.findNavController(this)
                    .popBackStack();
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.groupmenu, menu);
        /*
         * Possibly re-enable this. Currently I disabled it since the main menu has different items
         * than the group menu, and the 'Refresh' button won't work in the group.
         */
        // inflater.inflate(R.menu.mainmenu, menu);

        /*
         * If the current user is a moderator, display R.id.menu_group_moderator_actions
         * If the current user is an owner, display R.id.menu_group_owner_actions
         * and R.id.menu_group_moderator_actions
         */
        if (currentGroup.getGroupRole() == GroupRole.OWNER) {
            menu.setGroupVisible(R.id.menu_group_moderator_actions, true);
            menu.setGroupVisible(R.id.menu_group_owner_actions, true);
        }
        else if (currentGroup.getGroupRole() == GroupRole.MODERATOR) {
            menu.setGroupVisible(R.id.menu_group_moderator_actions, true);
            menu.setGroupVisible(R.id.menu_group_owner_actions, false);
        }
        else {
            menu.setGroupVisible(R.id.menu_group_moderator_actions, false);
            menu.setGroupVisible(R.id.menu_group_owner_actions, false);
        }
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

                new ViewMembersDialog(currentGroup.getUsers(), currentGroup.getGroupId(), 0)
                        .show(getParentFragmentManager(), null);
                return true;
            case R.id.leave_group:
                /** Handle leaving group **/

                ServerConnector.leaveGroup(currentGroup.getGroupId(), result -> {
                    if (result.isSuccess()) {
                        NavHostFragment.findNavController(GroupFragment.this)
                                .popBackStack();
                    }
                    else {
                        new StringErrorDialog(result.errorMessage)
                                .show(getParentFragmentManager(), null);
                    }
                });

                return true;
            case R.id.invite_member:
                /** Handle inviting a new member **/
                AddMemberDialog addMemberDialog = new AddMemberDialog(currentGroup.getGroupId(), R.string.invite_member);
                addMemberDialog.show(getParentFragmentManager(), null);
                return true;
            case R.id.add_moderator:
                /** Handle adding moderator **/
                ViewMembersDialog addModeratorDialog = new ViewMembersDialog(currentGroup.getUsers(), currentGroup.getGroupId(), 2);
                addModeratorDialog.show(getParentFragmentManager(), null);
                return true;
            case R.id.remove_moderator:
                /** Handle removing moderator **/
                ViewMembersDialog removeModeratorDialog = new ViewMembersDialog(currentGroup.getUsers(), currentGroup.getGroupId(), 3);
                removeModeratorDialog.show(getParentFragmentManager(), null);
                return true;
            case R.id.kick_member:
                /** Handle kicking member **/
                ViewMembersDialog kickUserDialog = new ViewMembersDialog(currentGroup.getUsers(), currentGroup.getGroupId(), 4);
                kickUserDialog.show(getParentFragmentManager(), null);
                return true;
            case R.id.mute_member:
                ViewMembersDialog muteDialog = new ViewMembersDialog(currentGroup.getUsers(), currentGroup.getGroupId(), 5);
                muteDialog.show(getParentFragmentManager(), null);
                return true;
            case R.id.unmute_member:
                ViewMembersDialog unmuteDialog = new ViewMembersDialog(currentGroup.getUsers(), currentGroup.getGroupId(), 6);
                unmuteDialog.show(getParentFragmentManager(), null);
                return true;
            case R.id.dis_enable_chat:
                /** Handle disabling/enabling chat **/
                return true;
            case R.id.transfer_ownership:
                /** Handle transfer ownership **/

                ViewMembersDialog transferOwnershipDialog = new ViewMembersDialog(currentGroup.getUsers(), currentGroup.getGroupId(), 1);
                transferOwnershipDialog.show(getParentFragmentManager(), null);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}