package com.gmail.comcorecrew.comcore.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.gmail.comcorecrew.comcore.R;

import java.util.Calendar;

public class PickDateTimeDialog extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {
    private final Fragment fragment;
    private final DateTimeCallback callback;
    private final boolean allowPast;
    private final long initialTime;

    public interface DateTimeCallback {
        void onSelected(Fragment fragment, long timestamp);
    }

    public PickDateTimeDialog(Fragment fragment, boolean allowPast, DateTimeCallback callback) {
        this(fragment, allowPast, 0, callback);
    }

    public PickDateTimeDialog(Fragment fragment, boolean allowPast, long initialTime,
                              DateTimeCallback callback) {
        if (fragment == null) {
            throw new IllegalArgumentException("Fragment cannot be null");
        } else if (initialTime < 0) {
            throw new IllegalArgumentException("initial timestamp cannot be negative");
        } else if (callback == null) {
            throw new IllegalArgumentException("DateTimeCallback cannot be null");
        }

        this.fragment = fragment;
        this.callback = callback;
        this.allowPast = allowPast;

        // Drop off any seconds on the time
        initialTime = initialTime / 60_000 * 60_000;
        if (initialTime != 0) {
            this.initialTime = initialTime;
            return;
        }

        // Round to the next hour
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE, 0);
        this.initialTime = calendar.getTimeInMillis();
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(initialTime);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DATE);
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(initialTime);
        calendar.set(year, month, day);

        if (!allowPast) {
            // Make sure the date is not in the past
            if (calendar.getTimeInMillis() + 24 * 60 * 60 * 1000 < System.currentTimeMillis()) {
                ErrorDialog.show(R.string.error_datetime_past);
                return;
            }
        }

        // Have the user pick the time next
        new PickTimeDialog(this, calendar)
                .show(fragment.getParentFragmentManager(), null);
    }

    public static class PickTimeDialog extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        private final PickDateTimeDialog parent;
        private final Calendar calendar;

        public PickTimeDialog(PickDateTimeDialog parent, Calendar calendar) {
            this.parent = parent;
            this.calendar = calendar;
        }

        @Override
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            long timestamp = calendar.getTimeInMillis();
            if (!parent.allowPast) {
                // Make sure the timestamp is not in the past
                if (timestamp < System.currentTimeMillis()) {
                    ErrorDialog.show(R.string.error_datetime_past);
                    return;
                }
            }

            // Call the provided callback
            parent.callback.onSelected(parent.fragment, timestamp);
        }
    }
}