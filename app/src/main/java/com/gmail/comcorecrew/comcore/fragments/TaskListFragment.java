package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.caching.TaskItem;
import com.gmail.comcorecrew.comcore.classes.modules.TaskList;
import com.gmail.comcorecrew.comcore.dialogs.CreateTaskDialog;
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.dialogs.ViewTasksDialog;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.TaskEntry;
import com.gmail.comcorecrew.comcore.server.id.TaskListID;

import java.util.ArrayList;

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

        /** Displays the name of the current group */
        TextView welcomeText = (TextView) view.findViewById(R.id.label_tasklist_fragment);
        welcomeText.setText(taskList.getName());

        /**
         * If the "back" button is clicked, return to the main page
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
     * Most menu items are not visible unless viewing GroupFragment
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {

            case R.id.refresh_button:
                /** Handle refresh **/
                taskList.refresh();
                return true;
            case R.id.settingsFragment:
                /** Handle moving to the settings page. The GroupID is passed to settings. */
                TaskListFragmentDirections.ActionTaskListFragmentToSettingsFragment action = TaskListFragmentDirections.actionTaskListFragmentToSettingsFragment(taskList.getGroup().getGroupId());
                action.setGroupId(taskList.getGroup().getGroupId());
                NavHostFragment.findNavController(TaskListFragment.this).navigate(action);
                return true;
            case R.id.create_task:
                /** Handle creating a new task **/
                CreateTaskDialog addTaskDialog = new CreateTaskDialog(taskList);
                addTaskDialog.show(getParentFragmentManager(), null);
                return true;
            case R.id.delete_task:
                ViewTasksDialog deleteTaskDialog = new ViewTasksDialog(taskList, 0);
                deleteTaskDialog.show(getParentFragmentManager(), null);
                return true;
            case R.id.update_task:
                ViewTasksDialog updateTaskDialog = new ViewTasksDialog(taskList, 1);
                updateTaskDialog.show(getParentFragmentManager(), null);
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
    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

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
            TaskEntry task = taskList.getEntry(position);
            dataText.setText(task.description);
            if (task.completed) {
                completedText.setText(R.string.completed);
            } else {
                completedText.setText(R.string.not_completed);
            }
        }

        @Override
        public int getItemCount() {
            return taskList.numEntries();
        }
    }
}
