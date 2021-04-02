package com.gmail.comcorecrew.comcore.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.GroupID;

import java.util.Calendar;

public class CreateLinkDialog extends DialogFragment {
    private final Fragment fragment;
    private final GroupID group;

    public CreateLinkDialog(Fragment fragment, GroupID group) {
        this.fragment = fragment;
        this.group = group;
    }

    private void finishLink(long expireTimestamp) {
        ServerConnector.createInviteLink(group, expireTimestamp, result -> {
            if (result.isFailure()) {
                new ErrorDialog(R.string.error_cannot_connect)
                        .show(fragment.getParentFragmentManager(), null);
                return;
            }

            // Create the link by adding HTTPS
            String link = "https://" + result.data;

            // Copy the link to the clipboard
            Context context = fragment.getContext();
            ClipboardManager clipboard = (ClipboardManager)
                    context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText(null, link));

            // Show the link in a dialog
            new StringErrorDialog("Link copied to clipboard:\n" + link)
                    .show(fragment.getParentFragmentManager(), null);
        });
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(R.string.create_link_does_expire)
                .setPositiveButton(R.string.yes, (dialog, id) ->
                    new PickDateDialog(this).show(fragment.getParentFragmentManager(), null))
                .setNegativeButton(R.string.no, (dialog, id) ->
                        finishLink(0))
                .setNeutralButton(R.string.cancel, null)
                .create();
    }

    public static class PickDateDialog extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private final CreateLinkDialog parent;

        public PickDateDialog(CreateLinkDialog parent) {
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

            // Make sure the date is not in the past
            long expireTimestamp = calendar.getTimeInMillis();
            if (expireTimestamp + 24 * 60 * 60 * 1000 < System.currentTimeMillis()) {
                new ErrorDialog(R.string.error_expire_past)
                        .show(parent.fragment.getParentFragmentManager(), null);
                return;
            }

            // Have the user pick the time next
            new PickTimeDialog(parent, calendar)
                    .show(parent.fragment.getParentFragmentManager(), null);
        }
    }

    public static class PickTimeDialog extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        private final CreateLinkDialog parent;
        private final Calendar calendar;

        public PickTimeDialog(CreateLinkDialog parent, Calendar calendar) {
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

            // Make sure the timestamp is not in the past
            long expireTimestamp = calendar.getTimeInMillis();
            if (expireTimestamp < System.currentTimeMillis()) {
                new ErrorDialog(R.string.error_expire_past)
                        .show(parent.fragment.getParentFragmentManager(), null);
                return;
            }

            // Finish creating the link
            parent.finishLink(calendar.getTimeInMillis());
        }
    }
}