package com.gmail.comcorecrew.comcore.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.classes.modules.TaskList;
import com.gmail.comcorecrew.comcore.server.entry.EventEntry;

import java.util.Date;

public class CreateTaskDialog extends DialogFragment {

    private TaskList currentTaskList;
    private long deadline = 0;
    private TextView deadlineLabel;

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

        deadlineLabel = view.findViewById(R.id.deadline_label);

        // Select an end date and time when the second select button is pressed
        view.findViewById(R.id.select_deadline_button).setOnClickListener(clickedView -> {
            long oldDeadline = deadline;
            deadline = 0;
            deadlineLabel.setText(R.string.no_deadline);
            new PickDateTimeDialog(this, false,
                    oldDeadline,
                    (fragment, timestamp) -> {
                        deadline = timestamp;

                        if (deadline == 0) {
                            deadlineLabel.setText(R.string.no_deadline);
                        } else {
                            deadlineLabel.setText(EventEntry.dateTimeFormat.format(new Date(deadline)));
                        }
                    })
                    .show(getParentFragmentManager(), null);
        });

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
            String name = taskDesc.getText().toString().trim();
            if (name.isEmpty()) {
                ErrorDialog.show("Please enter a name for the task.");
                return;
            }

            currentTaskList.sendTask(deadline, name);
            this.dismiss();
        });

    }
}
