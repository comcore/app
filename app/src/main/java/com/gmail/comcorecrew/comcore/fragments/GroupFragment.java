package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.TextView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.abstracts.CustomModule;
import com.gmail.comcorecrew.comcore.abstracts.Module;
import com.gmail.comcorecrew.comcore.caching.GroupStorage;
import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.classes.modules.Messaging;
import com.gmail.comcorecrew.comcore.classes.modules.TaskList;
import com.gmail.comcorecrew.comcore.dialogs.AddMemberDialog;
import com.gmail.comcorecrew.comcore.dialogs.CreateLinkDialog;
import com.gmail.comcorecrew.comcore.dialogs.CreateModuleDialog;
import com.gmail.comcorecrew.comcore.dialogs.StringErrorDialog;
import com.gmail.comcorecrew.comcore.dialogs.ViewMembersDialog;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.GroupID;

public class GroupFragment extends Fragment {

    private Group currentGroup;

    private CustomAdapter moduleAdapter;

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
        GroupID id = GroupFragmentArgs.fromBundle(getArguments()).getGroupID();
        currentGroup = AppData.getGroup(id);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_group, container, false);

        // Create the RecyclerView
        RecyclerView rvGroups = (RecyclerView) rootView.findViewById(R.id.group_recycler);
        rvGroups.setLayoutManager(new LinearLayoutManager(getActivity()));
        moduleAdapter = new CustomAdapter();
        rvGroups.setAdapter(moduleAdapter);
        rvGroups.setItemAnimator(new DefaultItemAnimator());

        return rootView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        refresh();

        TextView groupNameText = (TextView) view.findViewById(R.id.label_group_fragment);
        groupNameText.setText(currentGroup.getName());

        /**
         * If the "back" button is clicked, return to the main page
         */
        view.findViewById(R.id.group_back_button).setOnClickListener(clickedView -> {
            NavHostFragment.findNavController(this)
                    .popBackStack();
        });
    }

    public void refresh() {
        currentGroup.refreshModules(moduleAdapter::notifyDataSetChanged);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.groupmenu, menu);

        /**  TODO
         * If currentGroup is null, then there has been an error finding the group associated
         * with the GroupID passed to this function
         */
        if (currentGroup == null) {
            return;
        }
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
            case R.id.create_invite_link:
                // Handle creating an invite link
                CreateLinkDialog createLinkDialog = new CreateLinkDialog(this, currentGroup.getGroupId());
                createLinkDialog.show(getParentFragmentManager(), null);
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
            case R.id.create_module:
                CreateModuleDialog newModuleDialog = new CreateModuleDialog(currentGroup.getGroupId(), this);
                newModuleDialog.show(getParentFragmentManager(), null);
                return true;
            case R.id.transfer_ownership:
                /** Handle transfer ownership **/

                ViewMembersDialog transferOwnershipDialog = new ViewMembersDialog(currentGroup.getUsers(), currentGroup.getGroupId(), 1);
                transferOwnershipDialog.show(getParentFragmentManager(), null);

                return true;
            case R.id.create_sub_group:
                /** Handle creating sub group **/
                GroupFragmentDirections.ActionGroupFragmentToCreateGroupFragment action = GroupFragmentDirections.actionGroupFragmentToCreateGroupFragment(currentGroup.getGroupId());
                NavHostFragment.findNavController(GroupFragment.this).navigate(action);
                return true;
            case R.id.settingsFragment:
                /** Handle passing the current GroupID to the settings page */
                SettingsFragment.currentGroup = currentGroup;
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_groupFragment_to_settingsFragment);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * The GroupFragment uses the same style of RecyclerView that the MainFragment does to display
     * its list of groups.
     *
     * The CustomAdapter internal class sets up the RecyclerView, which displays
     * the list of modules in the GUI
     */
    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TextView textView;
            private Module currentModule;

            public ViewHolder(View view) {
                super(view);
                view.setOnClickListener(this);
                // Define click listener for the ViewHolder's View

                textView = (TextView) view.findViewById(R.id.module_row_text);

            }

            public TextView getTextView() {
                return textView;
            }

            public void setModule(Module currentModule) {
                this.currentModule = currentModule;
            }

            @Override
            public void onClick(View view) {
                if (currentModule instanceof CustomModule) {
                    CustomFragment.custom = (CustomModule) currentModule;
                    NavHostFragment.findNavController(GroupFragment.this)
                            .navigate(R.id.action_groupFragment_to_customFragment);
                }
                else if (currentModule instanceof Messaging) {
                    ChatFragment5.messaging = (Messaging) currentModule;
                    NavHostFragment.findNavController(GroupFragment.this)
                            .navigate(R.id.action_groupFragment_to_chatFragment5);
                } else if (currentModule instanceof TaskList) {
                    TaskListFragment.taskList = (TaskList) currentModule;
                    NavHostFragment.findNavController(GroupFragment.this)
                            .navigate(R.id.action_groupFragment_to_taskListFragment);
                }
            }
        }

        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.module_row_item, viewGroup, false);

            return new CustomAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CustomAdapter.ViewHolder viewHolder, final int position) {
            Module module = currentGroup.getModules().get(position);
            viewHolder.setModule(module);

            String name = module.getName();
            if (module instanceof Messaging) {
                viewHolder.getTextView().setText("Chat: " + name);
            } else if (module instanceof TaskList) {
                viewHolder.getTextView().setText("Task List: " + name);
            } else {
                viewHolder.getTextView().setText(name);
            }
        }

        @Override
        public int getItemCount() {
            return currentGroup.getModules().size();
        }
    }
}