package com.gmail.comcorecrew.comcore.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.classes.modules.Calendar;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.EventEntry;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.gmail.comcorecrew.comcore.server.id.PollListID;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CreateEventDialog extends DialogFragment {
    private final Fragment fragment;
    private final Calendar calendar;
    private final EventEntry oldEvent;


    public CreateEventDialog(Fragment fragment, Calendar calendar, EventEntry event) {
        this.fragment = fragment;
        this.calendar = calendar;
        this.oldEvent = event;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_create_event, container, false);

        return rootView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText desc = view.findViewById(R.id.create_event_desc);
        EditText startDate = view.findViewById(R.id.editStartDate);
        EditText endDate = view.findViewById(R.id.editEndDate);
        EditText startTime = view.findViewById(R.id.editStartTime);
        EditText endTime = view.findViewById(R.id.editEndTime);

        if (oldEvent != null) {
            desc.setText(oldEvent.description);
        }


        /**
          If the "cancel" button is clicked, close the dialog box
         */
        view.findViewById(R.id.create_event_cancel).setOnClickListener(clickedView -> {
            this.dismiss();
        });

        /**
         * If the "submit" button is clicked, create the event
         */
        view.findViewById(R.id.create_event_submit).setOnClickListener(clickedView -> {

            String startFull = startDate.getText().toString() + "-" + startTime.getText().toString();
            String endFull = endDate.getText().toString() + "-" + endTime.getText().toString();


            java.util.Calendar calStart = java.util.Calendar.getInstance();
            java.util.Calendar calEnd = java.util.Calendar.getInstance();

            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy-HH:mm");

            try {
                calStart.setTime(df.parse(startFull, new ParsePosition(0)));
                calEnd.setTime(df.parse(endFull, new ParsePosition(0)));
            }
            catch (NullPointerException e) {
                ErrorDialog.show(R.string.error_incorrect_format);
                this.dismiss();
            }

            /**
             * If an event was passed to the dialog, delete the old event and create a new one
             * to replace it
             */
            if (oldEvent != null) {
                calendar.deleteEvent(oldEvent.id);
            }

            /**
             * Users cannot automatically create events unless the group's settings allow it
             * **/
            calendar.sendEvent(desc.getText().toString(), calStart.getTimeInMillis(), calEnd.getTimeInMillis());
            this.dismiss();
        });

    }
}