package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.gmail.comcorecrew.comcore.abstracts.Module;
import com.gmail.comcorecrew.comcore.caching.TaskItem;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.classes.modules.TaskList;
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.enums.Mdid;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.TaskEntry;
import com.gmail.comcorecrew.comcore.server.id.ChatID;
import com.gmail.comcorecrew.comcore.server.id.ModuleID;
import com.gmail.comcorecrew.comcore.server.id.TaskListID;

import java.util.ArrayList;

public class TaskListFragment extends Fragment {

    private TaskList currentTaskList;
    private CustomAdapter tasklistAdapter;
    public static ArrayList<TaskItem> tasks =  new ArrayList<>();

    public TaskListFragment() {
        // Required empty public constructor
    }

    public static TaskListFragment newInstance() {
        TaskListFragment fragment = new TaskListFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        /** Retrieve the TaskList passed from GroupFragment
         */

        currentTaskList = TaskListFragmentArgs.fromBundle(getArguments()).getTaskList();
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

        return rootView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /** Displays the name of the current group */
        TextView welcomeText = (TextView) view.findViewById(R.id.label_tasklist_fragment);
        welcomeText.setText(currentTaskList.getName());

        /**
         * If the "back" button is clicked, return to the main page
         */
        view.findViewById(R.id.tasklist_back_button).setOnClickListener(clickedView -> {
            NavHostFragment.findNavController(this)
                    .popBackStack();
        });
    }


    /**
     * The TaskListFragment uses the same style of RecyclerView that the MainFragment does to display
     * its list of groups.
     *
     * The CustomAdapter internal class sets up the RecyclerView, which displays
     * the list of tasks in the GUI
     */
    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TextView textView;
            private TaskItem currentTaskItem;

            public ViewHolder(View view) {
                super(view);
                view.setOnClickListener(this);
                // Define click listener for the ViewHolder's View
                textView = (TextView) view.findViewById(R.id.task_description);

            }

            public TextView getTextView() {
                return textView;
            }

            public void setTaskItem(TaskItem currentTaskItem) {
                this.currentTaskItem = currentTaskItem;
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

            ServerConnector.getTasks((TaskListID) currentTaskList.getId(), result -> {
             if (result.isFailure()) {
             new ErrorDialog(R.string.error_cannot_connect)
             .show(getParentFragmentManager(), null);
             return;
             }

             TaskEntry[] info = result.data;
             ArrayList<TaskItem> serverTasks = new ArrayList<>();
             for (int i = 0; i < result.data.length; i++) {
             TaskItem nextTaskItem = new TaskItem(info[i]);
             serverTasks.add(nextTaskItem);
             }
             tasks = serverTasks;

             notifyDataSetChanged();
             });
        }

        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.tasklist_row_item, viewGroup, false);

            return new CustomAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CustomAdapter.ViewHolder viewHolder, final int position) {

            viewHolder.getTextView().setText(tasks.get(position).getData());
            viewHolder.setTaskItem(tasks.get(position));

        }

        @Override
        public int getItemCount() {
            return tasks.size();
        }
    }
}
