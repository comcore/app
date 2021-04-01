package com.gmail.comcorecrew.comcore.dialogs;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
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
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.fragments.MainFragment;
import com.gmail.comcorecrew.comcore.fragments.TaskListFragment;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.gmail.comcorecrew.comcore.server.id.TaskID;
import com.gmail.comcorecrew.comcore.server.id.TaskListID;

import java.util.ArrayList;

public class ViewTasksDialog extends DialogFragment {
    private RecyclerView recycleView;
    private CustomAdapter adapter;
    private ArrayList<TaskItem> taskList;
    private TaskListID currentTaskList;

    /** 0 - Delete Task
     * 1 - Update Task
     */
    private int flag;

    public ViewTasksDialog (ArrayList<TaskItem> taskList, TaskListID currentTaskList, int flag) {
        this.taskList = taskList;
        this.flag = flag;
        this.currentTaskList = currentTaskList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.view_tasks, container, false);

        // Create the RecyclerView
        RecyclerView rvGroups = (RecyclerView) rootView.findViewById(R.id.tasklist_recycler);
        rvGroups.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new CustomAdapter(taskList, flag);
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
            labelViewTasks.setText(R.string.update_task);
        }

        /**
         * If the "back" button is clicked, close the dialog box
         */
        view.findViewById(R.id.view_tasks_back_button).setOnClickListener(clickedView -> {
            this.dismiss();
        });

    }


    /** The CustomAdapter internal class sets up the RecyclerView, which displays
     * the list of members in the GUI
     */
    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

        private ArrayList<TaskItem> taskList;
        private int flag;

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TextView textView;
            private TaskItem currentTask;

            public ViewHolder(View view) {
                super(view);
                view.setOnClickListener(this);

                textView = (TextView) view.findViewById(R.id.label_member);
            }

            public TextView getTextView() {
                return textView;
            }

            public void setCurrentTask(TaskItem currentTask) {
                this.currentTask = currentTask;
            }

            @Override
            public void onClick(View view) {

            }
        }

        /**
         * Initialize the dataset of the Adapter.
         */
        public CustomAdapter(ArrayList<TaskItem> tasks, int flag) {

            this.taskList = tasks;
            this.flag = flag;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.member_row_item, viewGroup, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CustomAdapter.ViewHolder viewHolder, final int position) {

            TextView completedText = viewHolder.itemView.findViewById(R.id.task_completed_status);
            viewHolder.getTextView().setText(taskList.get(position).getData());
            if (taskList.get(position).isCompleted()) {
                completedText.setText(R.string.completed);
            }
            else {
                completedText.setText(R.string.not_completed);
            }
            viewHolder.setCurrentTask(taskList.get(position));

        }

        @Override
        public int getItemCount() {
            return taskList.size();
        }
    }

}

