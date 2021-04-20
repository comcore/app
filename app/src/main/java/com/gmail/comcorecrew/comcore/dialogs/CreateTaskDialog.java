package com.gmail.comcorecrew.comcore.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.classes.User;
import com.gmail.comcorecrew.comcore.classes.modules.TaskList;
import com.gmail.comcorecrew.comcore.fragments.GroupFragment;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.gmail.comcorecrew.comcore.server.id.TaskListID;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class CreateTaskDialog extends DialogFragment {

    private TaskListID tasklistID;
    private TaskList currentTaskList;

    public CreateTaskDialog (TaskList currentTasklist) {
        this.currentTaskList = currentTasklist;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_create_task, container, false);

        return rootView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        /*
          If the "back" button is clicked, close the dialog box
         */
        view.findViewById(R.id.create_task_back_button).setOnClickListener(clickedView -> {
            this.dismiss();
        });

        /*
          If the "submit" button is clicked, try to create the task
         */
        view.findViewById(R.id.create_task_submit_button).setOnClickListener(clickedView -> {

            EditText taskDesc = view.findViewById(R.id.create_task_name_edit);
            EditText dateDeadline = view.findViewById(R.id.editTaskDate);
            EditText timeDeadline = view.findViewById(R.id.editTaskTime);

            String startFull = dateDeadline.getText().toString() + "-" + timeDeadline.getText().toString();
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy-HH:mm");
            java.util.Calendar deadline = java.util.Calendar.getInstance();


            try {
                deadline.setTime(df.parse(startFull, new ParsePosition(0)));
                currentTaskList.sendTask(deadline.getTimeInMillis(), taskDesc.getText().toString());
            }
            catch (NullPointerException e) {
                /* If an error occurs parsing the deadline, create the task without a deadline */
                currentTaskList.sendTask(0, taskDesc.getText().toString());
            }
            this.dismiss();
        });

    }
}
