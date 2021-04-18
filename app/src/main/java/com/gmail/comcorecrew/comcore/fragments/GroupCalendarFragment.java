package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.classes.modules.Calendar;
import com.gmail.comcorecrew.comcore.dialogs.CreateEventDialog;
import com.gmail.comcorecrew.comcore.server.entry.EventEntry;
import com.gmail.comcorecrew.comcore.server.id.CalendarID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GroupCalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupCalendarFragment extends Fragment {

    public static Calendar calendar;
    private EventEntry eventEntry;
    private CalendarView calendarView;
    private Button add_event, remove_event;

    private  static final String TAG = "CalendarActivity";

    public GroupCalendarFragment() {
        // Required empty public constructor
    }

    public static GroupCalendarFragment newInstance(String param1, String param2) {
        GroupCalendarFragment fragment = new GroupCalendarFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragmen
        return inflater.inflate(R.layout.fragment_group_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calendarView = (CalendarView) view.findViewById(R.id.calendarView);
        add_event = (Button) view.findViewById(R.id.button_add_event);
        remove_event = (Button) view.findViewById(R.id.button_remove_event);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                String date = year + "/" + month + "/"+ dayOfMonth ;
                Log.d(TAG, "onSelectedDayChange: yyyy/mm/dd:" + date);
            }
        });

        add_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEvent();
            }
        });

        remove_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeEvent();
            }
        });
    }

    private void addEvent() {
        CreateEventDialog createEventDialog = new CreateEventDialog(this, calendar);
        createEventDialog.show(getParentFragmentManager(), null);
        calendar.refresh();
    }

    private void removeEvent() {
       //calendar.deleteEvent();
    }
}