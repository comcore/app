package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.classes.modules.Calendar;
import com.gmail.comcorecrew.comcore.dialogs.CreateEventDialog;
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.dialogs.ViewEventsDialog;
import com.gmail.comcorecrew.comcore.dialogs.ViewPendingEventsDialog;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.helpers.MessageListAdapter;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.EventEntry;
import com.gmail.comcorecrew.comcore.server.id.CalendarID;
import com.gmail.comcorecrew.comcore.server.id.ChatID;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GroupCalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupCalendarFragment extends Fragment {

    public static Calendar calendar;
    private CalendarView calendarView;
    private Toolbar toolBar;
    private CustomAdapter adapter;
    private List<EventEntry> eventEntries;
    private TextView textView;
    private RecyclerView rvGroups;
    private java.util.Calendar currentDay;
    private boolean pending = false;

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
        // Inflate the layout for this fragment
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        View rootView = inflater.inflate(R.layout.fragment_group_calendar, container, false);


//        RecyclerView rvGroups = (RecyclerView) rootView.findViewById(R.id.group_calendar_recyclerview);
//        rvGroups.setLayoutManager(new LinearLayoutManager(getContext()));
//        adapter = new CustomAdapter();
//        rvGroups.setAdapter(adapter);
//        rvGroups.setItemAnimator(new DefaultItemAnimator());


        return rootView;
        //return inflater.inflate(R.layout.fragment_group_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolBar = (Toolbar) view.findViewById(R.id.toolbar_group_calendar);
        toolBar.setTitle(calendar.getName());
        getActivity().setTitle(calendar.getName());
        calendarView = (CalendarView) view.findViewById(R.id.calendarView);

        textView = (TextView) view.findViewById(R.id.group_calendar_titleText);

        java.util.Calendar currentDate1 = java.util.Calendar.getInstance();
        currentDate1.setTime(new Date());

        rvGroups = (RecyclerView) view.findViewById(R.id.group_calendar_recyclerview);
        rvGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CustomAdapter();
        rvGroups.setAdapter(adapter);
        rvGroups.setItemAnimator(new DefaultItemAnimator());
        calendar.setCallback(this::refresh);

        if (calendar.getEntriesByDay(currentDate1, true).size() > 0) {
            textView.setText("Events on " + EventEntry.dateFormat.format(new Date(currentDate1.getTimeInMillis())));
            eventEntries = calendar.getEntriesByDay(currentDate1, true);
            refresh();
        } else {
            eventEntries = calendar.getEntriesByDay(currentDate1, false);
            textView.setText("Upcoming Events");
            refresh();
        }

        refresh();


        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int day) {
                java.util.Calendar currentDate = java.util.Calendar.getInstance();
                currentDate.set(java.util.Calendar.YEAR, year);
                currentDate.set(java.util.Calendar.MONTH, month);
                currentDate.set(java.util.Calendar.DATE, day);
                currentDate.set(java.util.Calendar.HOUR, 0);

                java.util.Calendar currentDate1 = java.util.Calendar.getInstance();
                currentDate1.setTime(new Date());

                if (calendar.getEntriesByDay(currentDate).size() > 0) {
                    textView.setText("Events on " + EventEntry.dateFormat.format(new Date(currentDate.getTimeInMillis())));
                    eventEntries = calendar.getEntriesByDay(currentDate);
                    refresh();
                } else {
                    eventEntries = calendar.getEntriesByDay(currentDate1);
                    textView.setText("Upcoming Events");
                    refresh();
                }
            }
        });
    }

    public void refresh(){
        adapter = new CustomAdapter();
        rvGroups.setAdapter(adapter);
        rvGroups.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onDestroy() {
        getActivity().setTitle("Comcore");
        super.onDestroy();
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
                new CreateEventDialog(this, calendar, null).show(getParentFragmentManager(), null);
                return true;
            case R.id.group_calendar_back_button:
                NavHostFragment.findNavController(this).popBackStack();
                return true;
            case R.id.group_calendar_view_all_events:
                eventEntries = calendar.getApproved();
                textView.setText("All Events");
                refresh();
                return true;
                //new ViewPendingEventsDialog(calendar).show(getParentFragmentManager(), null);
            case R.id.group_calendar_refresh:
                calendar.refresh();
                refresh();
                return true;
            case R.id.modify_event:
                /* Handle deleting calendar events*/
                new ViewEventsDialog(calendar, null, 3).show(getParentFragmentManager(), null);
                return true;
            case R.id.remove_event:
                /** Handle deleting calendar events**/
                new ViewEventsDialog(calendar, null, 1).show(getParentFragmentManager(), null);
                return true;
            case R.id.group_calendar_view_upcoming_events:
                java.util.Calendar currentDate = java.util.Calendar.getInstance();
                currentDate.setTime(new Date());

                textView.setText("Upcoming Events");
                eventEntries = calendar.getEntriesByDay(currentDay, false);
                refresh();
                return true;
            case R.id.view_pending_events:
                /** Handle view pending events **/
//                eventEntries = calendar.getUnapproved();
//                textView.setText("Pending Events");
//                refresh();
                //new ViewPendingEventsDialog(calendar).show(getParentFragmentManager(), null);
//                currentDay = null;
//                pending = true;
//                eventEntries = calendar.getUnapproved();
//                textView.setText("Here are all your pending events");
//                refresh();
                new ViewPendingEventsDialog(calendar).show(getParentFragmentManager(), null);
                currentDay = null;
                pending = true;
                refresh();
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



    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            //private final TextView textView;
            private EventEntry currentEventEntry;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
            }

            public void setCurrentEventEntry(EventEntry eventEntry) {
                this.currentEventEntry = eventEntry;
            }

            @Override
            public void onClick(View v) {

            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.subtitle_row_item, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {

            TextView eventDesc = viewHolder.itemView.findViewById(R.id.row_title);
            TextView eventDate = viewHolder.itemView.findViewById(R.id.row_subtitle);

            EventEntry event = eventEntries.get(position);
            eventDesc.setText(event.description);
            eventDate.setText(event.format(currentDay == null));

            viewHolder.setCurrentEventEntry(event);

        }

        @Override
        public int getItemCount() {
            return eventEntries.size();
        }
    }
}