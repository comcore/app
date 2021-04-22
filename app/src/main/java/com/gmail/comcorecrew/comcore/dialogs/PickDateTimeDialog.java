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

    public PickDateTimeDialog(Fragment fragment, DateTimeCallback callback, boolean allowPast) {
        this(fragment, callback, allowPast, System.currentTimeMillis());
    }

    public PickDateTimeDialog(Fragment fragment, DateTimeCallback callback, boolean allowPast,
                              long initialTime) {
        if (fragment == null) {
            throw new IllegalArgumentException("Fragment cannot be null");
        } else if (callback == null) {
            throw new IllegalArgumentException("DateTimeCallback cannot be null");
        }

        this.fragment = fragment;
        this.callback = callback;
        this.allowPast = allowPast;
        this.initialTime = initialTime;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(initialTime);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month, day);

        if (!allowPast) {
            // Make sure the date is not in the past
            if (calendar.getTimeInMillis() + 24 * 60 * 60 * 1000 < System.currentTimeMillis()) {
                ErrorDialog.show(R.string.error_expire_past);
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
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            if (!parent.allowPast) {
                // Make sure the timestamp is not in the past
                long expireTimestamp = calendar.getTimeInMillis();
                if (expireTimestamp < System.currentTimeMillis()) {
                    ErrorDialog.show(R.string.error_expire_past);
                    return;
                }
            }

            // Finish creating the link
            parent.callback.onSelected(parent.fragment, calendar.getTimeInMillis());
        }
    }
}