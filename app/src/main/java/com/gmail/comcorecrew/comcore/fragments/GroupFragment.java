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
import android.widget.ImageView;
import android.widget.TextView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.abstracts.Module;
import com.gmail.comcorecrew.comcore.caching.GroupStorage;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.dialogs.AddMemberDialog;
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.dialogs.StringErrorDialog;
import com.gmail.comcorecrew.comcore.dialogs.ViewMembersDialog;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.ChatID;
import com.gmail.comcorecrew.comcore.server.id.ModuleID;
import com.gmail.comcorecrew.comcore.server.info.GroupInfo;
import com.gmail.comcorecrew.comcore.server.info.ModuleInfo;

import java.util.ArrayList;
import java.util.Arrays;

public class GroupFragment extends Fragment {

    private Group currentGroup;
    public static ArrayList<Module> modules =  new ArrayList<>();

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

        /** Retrieve the GroupId passed from the main fragment and find
         * the Group associated with it.
         *
         * For this to work, AppData.init() must be run first.
         */

        /**GroupStorage.lookup(GroupFragmentArgs.fromBundle(getArguments()).getGroupID(), callback -> {
            currentGroup = callback;
        });*/

        /** TODO Testing only
         * A new group is created using the passed GroupID. This is only to test the functionality
         * of the modules and menus and should be replaced with the lookup function above once
         * AppData.init() is run in the app
         */
        currentGroup = new Group("Test Group", GroupFragmentArgs.fromBundle(getArguments()).getGroupID(), GroupRole.OWNER, Boolean.FALSE);
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

        view.findViewById(R.id.open_chat_button).setOnClickListener(clickedView -> {
            ServerConnector.getModules(currentGroup.getGroupId(), result -> {
                if (result.isFailure() || result.data.length == 0) {
                    System.out.println(result.data.length);
                    System.out.println(currentGroup.getGroupId());
                    return;
                }

                ModuleID id = result.data[0].id;
                if (id instanceof ChatID) {
                    /* Sends chatID and the current group to the chatFragment frame*/
                    ChatFragment5.chatID = (ChatID) id;
                    ChatFragment5.currentGroup = currentGroup;
//                    Intent i = new Intent(getActivity(), MessageListActivity.class);
//                    startActivity(i);
//                    ((Activity) getActivity()).overridePendingTransition(0, 0);

                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_groupFragment_to_chatFragment5);
                }
            });
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
//                ServerConnector.getModules(currentGroup.getGroupId(), result -> {
//                    if (result.isSuccess() && result.data.length == 0) {
//                        ServerConnector.createChat(currentGroup.getGroupId(), "General Chat", null);
//                    }
//                });
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

            }
        }

        public CustomAdapter() {
            refresh();
        }

        private void refresh() {
            /**
             * The modules ArrayList should be updated. If ModuleInfo is updated to return the Mdid,
             * then the commented out code can be run exactly like refresh() in MainFragment.
             *
             * If a single group can be updated from the server based on GroupId, then that
             * could be used instead. The list of modules could be retrieved from the updated group.
             */

            /**ServerConnector.getModules(currentGroup.getGroupId(), result -> {
                if (result.isFailure()) {
                    new ErrorDialog(R.string.error_cannot_connect)
                            .show(getParentFragmentManager(), null);
                    return;
                }

                ModuleInfo[] info = result.data;
                ArrayList<Module> groupModules = new ArrayList<>();
                for (int i = 0; i < result.data.length; i++) {
                    Module nextModule = new Module(info[i].name,  info[i].id, info[i].mdid);
                    groupModules.add(nextModule);
                }
                modules = groupModules;

                notifyDataSetChanged();
            });*/
        }

        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.module_row_item, viewGroup, false);

            return new CustomAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CustomAdapter.ViewHolder viewHolder, final int position) {

            viewHolder.getTextView().setText(modules.get(position).getName());
            viewHolder.setModule(modules.get(position));


        }

        @Override
        public int getItemCount() {
            return modules.size();
        }
    }
}