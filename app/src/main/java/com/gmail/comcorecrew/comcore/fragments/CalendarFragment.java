package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.classes.modules.Calendar;
import com.gmail.comcorecrew.comcore.dialogs.CreateEventDialog;
import com.gmail.comcorecrew.comcore.dialogs.ViewEventsDialog;
import com.gmail.comcorecrew.comcore.dialogs.ViewPendingEventsDialog;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.server.entry.EventEntry;
import com.gmail.comcorecrew.comcore.server.id.CalendarID;

import java.util.Date;


public class CalendarFragment extends Fragment {
    public static Calendar calendar;

    public CalendarFragment() {
        // Required empty public constructor
    }

    public static CalendarFragment newInstance() {
        return new CalendarFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_calendar, container, false);
        calendar.refresh();

        return rootView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /** Displays the name of the current calendar */
        TextView welcomeText = (TextView) view.findViewById(R.id.label_calendar_fragment);
        welcomeText.setText(calendar.getName());

        /**
         * If the "back" button is clicked, return to the main page
         */
        view.findViewById(R.id.calendar_back_button).setOnClickListener(clickedView -> {
            NavHostFragment.findNavController(this)
                    .popBackStack();
        });

        CalendarView calendarView = (CalendarView) view.findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int day) {

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.calendarmenu, menu);

        GroupRole role = calendar.getGroup().getGroupRole();
        menu.setGroupVisible(R.id.menu_group_moderator_actions, role != GroupRole.USER);
    }

    /**
     * Handles click events for the option menu
     * Most menu items are not visible unless viewing GroupFragment
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {

            case R.id.create_event:
                /** Handle creating an event **/
                new CreateEventDialog(this, calendar).show(getParentFragmentManager(), null);
                return true;
            case R.id.view_all_events:
                /** Handle viewing all calendar events**/
                new ViewEventsDialog(calendar, null, 0).show(getParentFragmentManager(), null);
                return true;
            case R.id.delete_events:
                /** Handle deleting calendar events**/
                new ViewEventsDialog(calendar, null, 1).show(getParentFragmentManager(), null);
                return true;
            case R.id.view_pending_events:
                /** Handle view pending events **/
                new ViewPendingEventsDialog(calendar).show(getParentFragmentManager(), null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}