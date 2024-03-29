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
import android.view.WindowManager;
import android.widget.TextView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.abstracts.CustomModule;
import com.gmail.comcorecrew.comcore.abstracts.Module;
import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.classes.modules.BulletinBoard;
import com.gmail.comcorecrew.comcore.classes.modules.Calendar;
import com.gmail.comcorecrew.comcore.classes.modules.Messaging;
import com.gmail.comcorecrew.comcore.classes.modules.Polling;
import com.gmail.comcorecrew.comcore.classes.modules.TaskList;
import com.gmail.comcorecrew.comcore.dialogs.AddMemberDialog;
import com.gmail.comcorecrew.comcore.dialogs.CreateLinkDialog;
import com.gmail.comcorecrew.comcore.dialogs.CreateModuleDialog;
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.dialogs.SelectMemberDialog;
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
        View rootView = inflater.inflate(R.layout.fragment_simple_recycler, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        // Create the RecyclerView
        RecyclerView rvGroups = (RecyclerView) rootView.findViewById(R.id.simple_recycler);
        rvGroups.setLayoutManager(new LinearLayoutManager(getActivity()));
        moduleAdapter = new CustomAdapter();
        rvGroups.setAdapter(moduleAdapter);
        rvGroups.setItemAnimator(new DefaultItemAnimator());

        return rootView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        refresh();

        TextView groupNameText = (TextView) view.findViewById(R.id.label_simple_fragment);
        groupNameText.setText(currentGroup.getDisplayName());

        /*
          If the "back" button is clicked, return to the main page
         */
        view.findViewById(R.id.simple_back_button).setOnClickListener(clickedView -> {
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

        /*  TODO
          If currentGroup is null, then there has been an error finding the group associated
          with the GroupID passed to this function
         */
        if (currentGroup == null) {
            return;
        }

        GroupRole role = currentGroup.getGroupRole();
        menu.setGroupVisible(R.id.menu_group_owner_actions, role == GroupRole.OWNER);

        boolean moderator = role != GroupRole.USER;
        menu.setGroupVisible(R.id.menu_group_moderator_actions, moderator);
        menu.setGroupVisible(R.id.menu_moderator_not_direct, moderator && !currentGroup.isDirect());
    }

    /**
     * Handles click events for the option menu
     * Most menu items are not visible unless viewing GroupFragment
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.view_members:
                /* Handle viewing list of members */

                new SelectMemberDialog(currentGroup).show(getParentFragmentManager(), null);
                return true;
            case R.id.leave_group:
                /* Handle leaving group */

                ServerConnector.leaveGroup(currentGroup.getGroupId(), result -> {
                    if (result.isSuccess()) {
                        NavHostFragment.findNavController(GroupFragment.this)
                                .popBackStack();
                    }
                    else {
                        ErrorDialog.show(result.errorMessage);
                    }
                });

                return true;
            case R.id.invite_member:
                /* Handle inviting a new member */
                AddMemberDialog addMemberDialog = new AddMemberDialog(currentGroup.getGroupId(), R.string.invite_member);
                addMemberDialog.show(getParentFragmentManager(), null);
                return true;
            case R.id.create_invite_link:
                // Handle creating an invite link
                CreateLinkDialog createLinkDialog = new CreateLinkDialog(this, currentGroup.getGroupId());
                createLinkDialog.show(getParentFragmentManager(), null);
                return true;
            case R.id.create_module:
                CreateModuleDialog newModuleDialog = new CreateModuleDialog(currentGroup.getGroupId(), this);
                newModuleDialog.show(getParentFragmentManager(), null);
                return true;
            case R.id.create_sub_group:
                /* Handle creating sub group */
                GroupFragmentDirections.ActionGroupFragmentToCreateGroupFragment action = GroupFragmentDirections.actionGroupFragmentToCreateGroupFragment(currentGroup.getGroupId());
                NavHostFragment.findNavController(GroupFragment.this).navigate(action);
                return true;
            case R.id.settingsFragment:
                /* Handle passing the current GroupID to the settings page */
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

                textView = (TextView) view.findViewById(R.id.title_row_text);

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
                } //else if (currentModule instanceof Calendar) {
                   // GroupCalendarFragment.calendar = (Calendar) currentModule;
                    //NavHostFragment.findNavController(GroupFragment.this).navigate(R.id.action_groupFragment_to_calendarFragment);
               // }
                else if (currentModule instanceof Calendar) {
                    CalendarFragment.calendar = (Calendar) currentModule;
                    GroupCalendarFragment.calendar = (Calendar) currentModule;
                    NavHostFragment.findNavController(GroupFragment.this)
                            .navigate(R.id.action_groupFragment_to_calendarFragment);
                }
                else if (currentModule instanceof Polling) {
                    PollingFragment.polling = (Polling) currentModule;
                    NavHostFragment.findNavController(GroupFragment.this)
                            .navigate(R.id.action_groupFragment_to_pollingFragment);
                }
                else if (currentModule instanceof BulletinBoard) {
                    CustomFragment.custom = (BulletinBoard) currentModule;
                    NavHostFragment.findNavController(GroupFragment.this)
                            .navigate(R.id.action_groupFragment_to_customFragment);
                }
            }
        }

        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.title_row_item, viewGroup, false);

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
            } else if (module instanceof Calendar) {
                viewHolder.getTextView().setText("Calendar: " + name);
            } else if (module instanceof Polling) {
                viewHolder.getTextView().setText("Polls: " + name);
            } else if (module instanceof BulletinBoard) {
                viewHolder.getTextView().setText("Bulletin Board: " + name);
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