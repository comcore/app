package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.classes.modules.Calendar;
import com.gmail.comcorecrew.comcore.dialogs.CreateEventDialog;
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.dialogs.ViewEventsDialog;
import com.gmail.comcorecrew.comcore.dialogs.ViewPendingEventsDialog;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.EventEntry;
import com.gmail.comcorecrew.comcore.server.id.CalendarID;
import com.gmail.comcorecrew.comcore.server.id.ChatID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GroupCalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupCalendarFragment extends Fragment {

    public static Calendar calendar;
    private CalendarView calendarView;
    private Toolbar toolBar;

    private  static final String TAG = "CalendarActivity";

    public GroupCalendarFragment() {
        // Required empty public constructor
    }

    public static GroupCalendarFragment newInstance(String param1, String param2) {
        GroupCalendarFragment fragment = new GroupCalendarFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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

        toolBar = (Toolbar) view.findViewById(R.id.toolbar_group_calendar);
        toolBar.setTitle(calendar.getName());
        getActivity().setTitle(calendar.getName());

        calendarView = (CalendarView) view.findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int day) {
                java.util.Calendar currentDate = java.util.Calendar.getInstance();
                currentDate.set(java.util.Calendar.YEAR, year);
                currentDate.set(java.util.Calendar.MONTH, month);
                currentDate.set(java.util.Calendar.DATE, day);
                currentDate.set(java.util.Calendar.HOUR, 0);

                if (calendar.getEntriesByDay(currentDate).size() > 0) {
                    new ViewEventsDialog(calendar, currentDate, 0).show(getParentFragmentManager(), null);
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.groupcalendarmenu, menu);

        GroupRole role = calendar.getGroup().getGroupRole();
        menu.setGroupVisible(R.id.menu_group_moderator_actions, role != GroupRole.USER);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.create_event:
                /** Handle creating an event **/
                new CreateEventDialog(this, calendar).show(getParentFragmentManager(), null);
                return true;
            case R.id.view_events:
                /** Handle viewing all calendar events**/
                new ViewEventsDialog(calendar, null, 0).show(getParentFragmentManager(), null);
                return true;
            case R.id.remove_event:
                /** Handle deleting calendar events**/
                new ViewEventsDialog(calendar, null, 1).show(getParentFragmentManager(), null);
                return true;
            case R.id.view_pending_events:
                /** Handle view pending events **/
                new ViewPendingEventsDialog(calendar).show(getParentFragmentManager(), null);
                return true;
            case R.id.pin_event:
                /** Handle pinning event to the bulletin board **/
                new ViewEventsDialog(calendar, null, 2).show(getParentFragmentManager(), null);
                return true;
            case R.id.require_approval:
                /** Handle updating event approval settings **/
                calendar.getGroup().updateRequireApproval(!calendar.getGroup().isRequireApproval());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}