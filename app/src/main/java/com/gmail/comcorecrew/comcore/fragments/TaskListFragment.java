package com.gmail.comcorecrew.comcore.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.caching.UserStorage;
import com.gmail.comcorecrew.comcore.classes.modules.TaskList;
import com.gmail.comcorecrew.comcore.dialogs.CreateTaskDialog;
import com.gmail.comcorecrew.comcore.dialogs.ViewTasksDialog;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.enums.TaskStatus;
import com.gmail.comcorecrew.comcore.server.entry.EventEntry;
import com.gmail.comcorecrew.comcore.server.entry.TaskEntry;

import java.util.Calendar;
import java.util.Date;

public class TaskListFragment extends Fragment {
    public static TaskList taskList;
    private CustomAdapter tasklistAdapter;

    public TaskListFragment() {
        // Required empty public constructor
    }

    public static TaskListFragment newInstance() {
        return new TaskListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_tasklist, container, false);

        // Create the RecyclerView
        RecyclerView rvGroups = (RecyclerView) rootView.findViewById(R.id.tasklist_recycler);
        rvGroups.setLayoutManager(new LinearLayoutManager(getActivity()));
        tasklistAdapter = new TaskListFragment.CustomAdapter();
        rvGroups.setAdapter(tasklistAdapter);
        rvGroups.setItemAnimator(new DefaultItemAnimator());

        taskList.setCallback(this::refresh);
        taskList.refresh();

        return rootView;
    }

    public void refresh() {
        tasklistAdapter.notifyDataSetChanged();
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /* Displays the name of the current group */
        TextView welcomeText = (TextView) view.findViewById(R.id.label_tasklist_fragment);
        welcomeText.setText(taskList.getName());

        /*
          If the "back" button is clicked, return to the main page
         */
        view.findViewById(R.id.tasklist_back_button).setOnClickListener(clickedView -> {
            NavHostFragment.findNavController(this)
                    .popBackStack();
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.taskmenu, menu);
    }

    /**
     * Handles click events for the option menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {

            case R.id.refresh_button:
                /* Handle refresh */
                taskList.refresh();
                return true;
            case R.id.settingsFragment:
                /* Handle moving to the settings page */
                SettingsFragment.currentGroup = taskList.getGroup();
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_taskListFragment_to_settingsFragment);
                return true;
            case R.id.create_task:
                /* Handle creating a new task */
                CreateTaskDialog addTaskDialog = new CreateTaskDialog(taskList);
                addTaskDialog.show(getParentFragmentManager(), null);
                return true;
            case R.id.delete_task:
                ViewTasksDialog deleteTaskDialog = new ViewTasksDialog(taskList, 0);
                deleteTaskDialog.show(getParentFragmentManager(), null);
                return true;
            case R.id.complete_task:
                ViewTasksDialog completeTaskDialog = new ViewTasksDialog(taskList, 1);
                completeTaskDialog.show(getParentFragmentManager(), null);
                return true;
            case R.id.progress_task:
                ViewTasksDialog progressTaskDialog = new ViewTasksDialog(taskList, 2);
                progressTaskDialog.show(getParentFragmentManager(), null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * The TaskListFragment uses the same style of RecyclerView that the MainFragment does to display
     * its list of groups.
     *
     * The CustomAdapter internal class sets up the RecyclerView, which displays
     * the list of tasks in the GUI
     */
    public static class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(View view) {
                super(view);
            }
        }

        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.tasklist_row_item, viewGroup, false);

            return new CustomAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CustomAdapter.ViewHolder viewHolder, final int position) {
            TextView dataText = viewHolder.itemView.findViewById(R.id.task_description);
            TextView completedText = viewHolder.itemView.findViewById(R.id.task_completed_status);
            TextView deadlineText = viewHolder.itemView.findViewById(R.id.task_deadline);

            TaskEntry task = taskList.getEntry(position);
            dataText.setText(task.description);
            /** Only moderators can see the user who completed task items **/
            if ((taskList.getGroup().getGroupRole() == GroupRole.USER) && (task.getStatus() == TaskStatus.COMPLETED)) {
                completedText.setText(R.string.completed);
            }
            else {
                completedText.setText(task.getStatusDescription());
            }

            if (task.hasDeadline()) {
                String parsedDate = "Deadline: " + EventEntry.dateTimeFormat.format(new Date(task.deadline));
                deadlineText.setText(parsedDate);
                if(task.deadline - Calendar.getInstance().getTimeInMillis() < 86400000) {
                    deadlineText.setTextColor(Color.RED);
                }
            }
            else {
                deadlineText.setText(R.string.no_deadline);
            }
        }

        @Override
        public int getItemCount() {
            return taskList.numEntries();
        }
    }
}