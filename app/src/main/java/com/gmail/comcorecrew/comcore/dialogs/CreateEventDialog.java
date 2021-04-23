package com.gmail.comcorecrew.comcore.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.classes.modules.Calendar;
import com.gmail.comcorecrew.comcore.server.entry.EventEntry;

import java.util.Date;

public class CreateEventDialog extends DialogFragment {
    private final Fragment fragment;
    private final Calendar calendar;
    private long startTimestamp = 0;
    private long endTimestamp = 0;
    TextView startLabel, endLabel;

    public CreateEventDialog(Fragment fragment, Calendar calendar) {
        this.fragment = fragment;
        this.calendar = calendar;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_event, container, false);
    }

    private void updateLabels() {
        if (startTimestamp == 0) {
            startLabel.setText(R.string.no_time_selected);
        } else {
            startLabel.setText(EventEntry.dateTimeFormat.format(new Date(startTimestamp)));
        }

        if (endTimestamp == 0) {
            endLabel.setText(R.string.no_time_selected);
        } else {
            endLabel.setText(EventEntry.dateTimeFormat.format(new Date(endTimestamp)));
        }
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText desc = view.findViewById(R.id.create_event_desc);
        startLabel = view.findViewById(R.id.start_time_label);
        endLabel = view.findViewById(R.id.end_time_label);

        // Select a start date and time when the first select button is pressed
        view.findViewById(R.id.select_start_button).setOnClickListener(clickedView -> {
            new PickDateTimeDialog(this, true,
                    startTimestamp == 0 ? endTimestamp : startTimestamp,
                    (fragment, timestamp) -> {
                        startTimestamp = timestamp;
                        if (endTimestamp != 0 && endTimestamp < startTimestamp) {
                            endTimestamp = startTimestamp;
                        }
                        updateLabels();
                    })
                    .show(getParentFragmentManager(), null);
        });

        // Select an end date and time when the second select button is pressed
        view.findViewById(R.id.select_end_button).setOnClickListener(clickedView -> {
            new PickDateTimeDialog(this, true,
                    endTimestamp == 0 ? startTimestamp : endTimestamp,
                    (fragment, timestamp) -> {
                        endTimestamp = timestamp;
                        if (startTimestamp != 0 && startTimestamp > endTimestamp ) {
                            startTimestamp = endTimestamp;
                        }
                        updateLabels();
                    })
                    .show(getParentFragmentManager(), null);
        });

        // Close the dialog if cancel is selected
        view.findViewById(R.id.create_event_cancel).setOnClickListener(clickedView -> {
            this.dismiss();
        });

        // Submit the information if submit is selected
        view.findViewById(R.id.create_event_submit).setOnClickListener(clickedView -> {
            String name = desc.getText().toString().trim();
            if (name.isEmpty()) {
                ErrorDialog.show("Please enter a name for the event.");
                return;
            } else if (startTimestamp == 0) {
                ErrorDialog.show("Please select a start time.");
                return;
            } else if (endTimestamp == 0) {
                ErrorDialog.show("Please select an end time.");
                return;
            }

            calendar.sendEvent(name, startTimestamp, endTimestamp);
            this.dismiss();
        });

    }
}