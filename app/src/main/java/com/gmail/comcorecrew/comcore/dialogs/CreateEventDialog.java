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
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.CalendarID;
import com.gmail.comcorecrew.comcore.server.id.GroupID;

import java.util.Calendar;

public class CreateEventDialog extends DialogFragment {
    private final Fragment fragment;
    private final CalendarID calendarID;

    public CreateEventDialog(Fragment fragment, CalendarID calendarID) {
        this.fragment = fragment;
        this.calendarID = calendarID;
    }

    private void createEvent(long expireTimestamp) {

        EditText createEventDesc = getView().findViewById(R.id.create_event_desc);

        /** If the user is a moderator or an owner, the event can be created **/
        if (AppData.getGroup(calendarID.group).getGroupRole() != GroupRole.USER) {
            ServerConnector.addEvent(calendarID, expireTimestamp, createEventDesc.getText().toString(), result -> {
                if (result.isFailure()) {
                    new ErrorDialog(R.string.error_cannot_connect)
                            .show(fragment.getParentFragmentManager(), null);
                    return;
                }

                this.dismiss();
            });
        }
        /** TODO If the user is a normal group user, the event must be sent to a moderator **/
        else {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_create_event, container, false);

        return rootView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /**
         * If the "cancel" button is clicked, close the dialog box
         */
        view.findViewById(R.id.create_event_cancel).setOnClickListener(clickedView -> {
            this.dismiss();
        });

        /**
         * If the "pick a date" button is clicked, show the PickDateDialog
         */
        view.findViewById(R.id.pick_date_button).setOnClickListener(clickedView -> {
            new PickDateDialog(this).show(fragment.getParentFragmentManager(), null);
        });
    }

    public static class PickDateDialog extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private final CreateEventDialog parent;

        public PickDateDialog(CreateEventDialog parent) {
            this.parent = parent;
        }

        @Override
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.set(year, month, day);

            long expireTimestamp = calendar.getTimeInMillis();
            if (expireTimestamp + 24 * 60 * 60 * 1000 < System.currentTimeMillis()) {
                new ErrorDialog(R.string.error_expire_past)
                        .show(parent.fragment.getParentFragmentManager(), null);
                return;
            }

            new PickTimeDialog(parent, calendar)
                    .show(parent.fragment.getParentFragmentManager(), null);
        }
    }

    public static class PickTimeDialog extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        private final CreateEventDialog parent;
        private final Calendar calendar;

        public PickTimeDialog(CreateEventDialog parent, Calendar calendar) {
            this.parent = parent;
            this.calendar = calendar;
        }

        @Override
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            long expireTimestamp = calendar.getTimeInMillis();
            if (expireTimestamp < System.currentTimeMillis()) {
                new ErrorDialog(R.string.error_expire_past)
                        .show(parent.fragment.getParentFragmentManager(), null);
                return;
            }

            /** Create the event **/
            parent.createEvent(calendar.getTimeInMillis());
        }
    }
}