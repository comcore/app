package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.classes.modules.Calendar;
import com.gmail.comcorecrew.comcore.dialogs.ViewEventsDialog2;

public class SharedCalendarFragment2 extends Fragment {
    public static Calendar calendar;
    private CalendarView calendarView;
    private Toolbar toolBar;


    public SharedCalendarFragment2() {
        // Required empty public constructor
    }

    public static SharedCalendarFragment2 newInstance() {
        return new SharedCalendarFragment2();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_calendar, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolBar = (Toolbar) view.findViewById(R.id.toolbar_group_calendar);
        toolBar.setTitle("Shared Calendar");
        getActivity().setTitle("Shared Calendar");
        calendarView = (CalendarView) view.findViewById(R.id.calendarView);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int day) {
                java.util.Calendar currentDate = java.util.Calendar.getInstance();
                currentDate.set(java.util.Calendar.YEAR, year);
                currentDate.set(java.util.Calendar.MONTH, month);
                currentDate.set(java.util.Calendar.DATE, day);
                currentDate.set(java.util.Calendar.HOUR, 0);

//                if (calendar.getEntriesByDay(currentDate).size() > 0) {
//                    new ViewEventsDialog2(currentDate).show(getParentFragmentManager(), null);
//                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.sharedchatmenu, menu);

    }

    @Override
    public void onDestroy() {
        getActivity().setTitle("Comcore");
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.view_events:
                System.out.println("Why wont this work?");
                /** Handle viewing all calendar events**/
                new ViewEventsDialog2(null).show(getParentFragmentManager(), null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
