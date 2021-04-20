package com.gmail.comcorecrew.comcore.dialogs;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.caching.TaskItem;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.classes.User;
import com.gmail.comcorecrew.comcore.classes.modules.TaskList;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.fragments.MainFragment;
import com.gmail.comcorecrew.comcore.fragments.TaskListFragment;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.TaskEntry;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.gmail.comcorecrew.comcore.server.id.TaskID;
import com.gmail.comcorecrew.comcore.server.id.TaskListID;

import java.util.ArrayList;

public class ViewTasksDialog extends DialogFragment {

    private TaskList currentTaskList;

    /** 0 - Delete Task
     * 1 - Complete Task
     * 2 - Work On Task
     */
    private int flag;

    public ViewTasksDialog (TaskList currentTaskList, int flag) {
        this.currentTaskList = currentTaskList;
        this.flag = flag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.view_tasks, container, false);

        // Create the RecyclerView
        RecyclerView rvGroups = (RecyclerView) rootView.findViewById(R.id.view_tasks_recycler);
        rvGroups.setLayoutManager(new LinearLayoutManager(getActivity()));
        ViewTasksDialog.CustomAdapter adapter = new ViewTasksDialog.CustomAdapter();
        rvGroups.setAdapter(adapter);
        rvGroups.setItemAnimator(new DefaultItemAnimator());

        return rootView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView labelViewTasks = view.findViewById(R.id.label_view_tasks);

        if (flag == 0) {
            labelViewTasks.setText(R.string.delete_task);
        }
        else if (flag == 1) {
            labelViewTasks.setText(R.string.complete_task);
        }
        else if (flag == 2) {
            labelViewTasks.setText(R.string.in_progress_task);
        }

        /*
          If the "back" button is clicked, close the dialog box
         */
        view.findViewById(R.id.view_tasks_back_button).setOnClickListener(clickedView -> {
            this.dismiss();
        });

    }


    /** The CustomAdapter internal class sets up the RecyclerView, which displays
     * the list of members in the GUI
     */
    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TextView textView;
            private TaskEntry currentTask;

            public ViewHolder(View view) {
                super(view);
                view.setOnClickListener(this);

                textView = (TextView) view.findViewById(R.id.label_member);
            }

            public TextView getTextView() {
                return textView;
            }

            public void setCurrentTask(TaskEntry currentTask) {
                this.currentTask = currentTask;
            }

            @Override
            public void onClick(View view) {
                if (flag == 0) {
                    currentTaskList.deleteTask(currentTask.id);
                } else if (flag == 1) {
                    currentTaskList.toggleCompleted(currentTask.id);
                } else if (flag == 2) {
                    switch (currentTask.getStatus()) {
                        case COMPLETED:
                            ErrorDialog.show(R.string.error_already_complete);
                            break;
                        default:
                            currentTaskList.toggleAssigned(currentTask.id);
                            break;
                    }
                }
                dismiss();
            }
        }

        /**
         * Initialize the dataset of the Adapter.
         */
        public CustomAdapter() {

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.tasklist_row_item, viewGroup, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CustomAdapter.ViewHolder viewHolder, final int position) {

            TextView dataText = viewHolder.itemView.findViewById(R.id.task_description);
            TextView completedText = viewHolder.itemView.findViewById(R.id.task_completed_status);
            TextView deadlineText = viewHolder.itemView.findViewById(R.id.task_deadline);

            TaskEntry task = currentTaskList.getEntry(position);
            dataText.setText(task.description);
            completedText.setText(task.getStatusDescription());
            if (task.hasDeadline()) {
                String parsedDate = "Deadline: " + DateFormat.format("MM-dd-yyyy HH:mm", task.deadline).toString();
                deadlineText.setText(parsedDate);
            }
            else {
                deadlineText.setText(R.string.no_deadline);
            }
            viewHolder.setCurrentTask(task);
        }

        @Override
        public int getItemCount() {
            return currentTaskList.numEntries();
        }
    }

}