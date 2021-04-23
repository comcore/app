package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.classes.modules.Calendar;
import com.gmail.comcorecrew.comcore.dialogs.ViewEventsDialog2;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.EventEntry;
import com.gmail.comcorecrew.comcore.server.id.CalendarID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SharedCalendarFragment2 extends Fragment {
    public static Calendar calendar;
    private CalendarView calendarView;
    private Toolbar toolBar;
    List<EventEntry> eventEntries = new ArrayList<>();
    private List<EventEntry> eventEntries1 = new ArrayList<>();
    private CustomAdapter adapter;
    private TextView textView;
    private RecyclerView rvGroups;


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

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_calendar, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolBar = (Toolbar) view.findViewById(R.id.toolbar_group_calendar);
        toolBar.setTitle("Shared Calendar");
        getActivity().setTitle("Shared Calendar");
        calendarView = (CalendarView) view.findViewById(R.id.calendarView);

        rvGroups = (RecyclerView) view.findViewById(R.id.group_calendar_recyclerview);
        rvGroups.setLayoutManager(new LinearLayoutManager(getContext()));

        textView = (TextView) view.findViewById(R.id.group_calendar_titleText);
        textView.setText("Upcoming Events");

        ServerConnector.getGroups(result -> {
            for (int i = 0; i < result.data.length; i++) {
                ServerConnector.getModules(result.data[i], result1 -> {
                    for (int j = 0; j < result1.data.length; j++) {
                        if (result1.data[j].id instanceof CalendarID) {
                            ServerConnector.getEvents((CalendarID) result1.data[j].id, result2 -> {
                                java.util.Calendar currentDate = java.util.Calendar.getInstance();
                                currentDate.setTime(new Date());

                                for (int k = 0; k < result2.data.length; k++) {
                                    eventEntries.add(result2.data[k]);
                                }
                                eventEntries1 = eventEntries;

                                if (getEvents(currentDate, eventEntries, true).size() > 0) {
                                    textView.setText("Events on " + EventEntry.dateTimeFormat.format(new Date(currentDate.getTimeInMillis())));
                                    eventEntries1 = getEvents(currentDate, eventEntries, true);
                                    refresh();
                                    //new ViewEventsDialog2(currentDate, getEvents(currentDate, eventEntries)).show(getParentFragmentManager(), null);
                                } else {
                                    System.out.println("Failed TRUE");
                                    textView.setText("Upcoming Events");
                                    //eventEntries1 = eventEntries;
                                    eventEntries1 = getEvents(currentDate, eventEntries, false);
                                    refresh();
                                }
                            });
                        }
                    }
                });
            }
        });

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int day) {
                java.util.Calendar currentDate = java.util.Calendar.getInstance();
                currentDate.clear();
                currentDate.set(year, month, day);

                java.util.Calendar currentDate1 = java.util.Calendar.getInstance();
                currentDate1.setTime(new Date());

                if (getEvents(currentDate, eventEntries, true).size() > 0) {
                    textView.setText("Events on " + EventEntry.dateTimeFormat.format(new Date(currentDate.getTimeInMillis())));
                    eventEntries1 = getEvents(currentDate, eventEntries, true);
                    refresh();
                    //new ViewEventsDialog2(currentDate, getEvents(currentDate, eventEntries)).show(getParentFragmentManager(), null);
                } else {
                    System.out.println("Failed TRUE");
                    textView.setText("Upcoming Events");
                    //eventEntries1 = eventEntries;
                    eventEntries1 = getEvents(currentDate1, eventEntries, false);
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

    public List<EventEntry> getEvents(java.util.Calendar currentDay, List<EventEntry> eventEntries, boolean bool) {
        if (!bool) {
            if (currentDay == null) {
                return null;
            }

            ArrayList<EventEntry> eventList = new ArrayList<>();

            java.util.Calendar startDay = java.util.Calendar.getInstance();
            java.util.Calendar endDay = java.util.Calendar.getInstance();

            for (int i = 0; i < eventEntries.size(); i++) {
                startDay.setTimeInMillis(eventEntries.get(i).start);
                endDay.setTimeInMillis(eventEntries.get(i).end);

                /* Currently gets entries based on their starting day
             TODO match entries as long as the currentDay overlaps with its time range */
                if (currentDay.get(java.util.Calendar.YEAR) >= endDay.get(java.util.Calendar.YEAR) &&
                        currentDay.get(java.util.Calendar.MONTH) >= endDay.get(java.util.Calendar.MONTH) &&
                        currentDay.get(java.util.Calendar.DATE) >= endDay.get(java.util.Calendar.DATE)) {
                } else {
                    eventList.add(eventEntries.get(i));
                }

                Collections.sort(eventList, (a, b) -> {
                    int result = Long.compare(a.start, b.start);
                    if (result != 0) {
                        return result;
                    }

                        result = Long.compare(b.end, a.end);
                        if (result != 0) {
                            return result;
                        }

                        return a.description.compareTo(b.description);
                    });


                }
                return eventList;
        }
        if (bool) {
            if (currentDay == null) {
                return null;
            }

            ArrayList<EventEntry> eventList = new ArrayList<>();

            java.util.Calendar startDay = java.util.Calendar.getInstance();
            java.util.Calendar endDay = java.util.Calendar.getInstance();


            for (int i = 0; i < eventEntries.size(); i++) {
                startDay.setTimeInMillis(eventEntries.get(i).start);
                endDay.setTimeInMillis(eventEntries.get(i).end);
            /* Currently gets entries based on their starting day
              TODO match entries as long as the currentDay overlaps with its time range */
                if (currentDay.get(java.util.Calendar.YEAR) == startDay.get(java.util.Calendar.YEAR) &&
                        currentDay.get(java.util.Calendar.MONTH) == startDay.get(java.util.Calendar.MONTH) &&
                        currentDay.get(java.util.Calendar.DATE) == startDay.get(java.util.Calendar.DATE)) {

                    eventList.add(eventEntries.get(i));
                } else if (currentDay.getTimeInMillis() <= endDay.getTimeInMillis() && currentDay.getTimeInMillis() >= startDay.getTimeInMillis()) {
                    eventList.add(eventEntries.get(i));
                } else if (currentDay.get(java.util.Calendar.YEAR) == endDay.get(java.util.Calendar.YEAR) &&
                        currentDay.get(java.util.Calendar.MONTH) == endDay.get(java.util.Calendar.MONTH) &&
                        currentDay.get(java.util.Calendar.DATE) == endDay.get(java.util.Calendar.DATE)) {
                    eventList.add(eventEntries.get(i));
                }

                Collections.sort(eventList, (a, b) -> {
                    int result = Long.compare(a.start, b.start);
                    if (result != 0) {
                        return result;
                    }

                    result = Long.compare(b.end, a.end);
                    if (result != 0) {
                        return result;
                    }

                    return a.description.compareTo(b.description);
                });
            }
            return eventList;
        }
        return null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.sharedcalendarmenu, menu);
    }

    @Override
    public void onDestroy() {
        getActivity().setTitle("Comcore");
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.shared_calendar_back_button:
                /** Handle viewing all calendar events**/
                NavHostFragment.findNavController(this).popBackStack();
                return true;
            case R.id.shared_calendar_view_upcoming_events:
                java.util.Calendar currentDate = java.util.Calendar.getInstance();
                currentDate.setTime(new Date());

                textView.setText("Here are all your upcoming events");
                eventEntries1 = getEvents(currentDate, eventEntries, false);
                refresh();
                return true;
            case R.id.shared_calendar_view_all_events:
                textView.setText("Here are all of your events");
                eventEntries1 = eventEntries;
                refresh();
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
                //textView = (TextView) itemView.findViewById(R.id.label_invite);
            }

//            public TextView getTextView() {
//                return textView;
//            }

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

            EventEntry event = eventEntries1.get(position);
            eventDesc.setText(event.description);
            eventDate.setText(event.format(true));

            viewHolder.setCurrentEventEntry(event);

        }

        @Override
        public int getItemCount() {
            return eventEntries1.size();
        }
    }
}
