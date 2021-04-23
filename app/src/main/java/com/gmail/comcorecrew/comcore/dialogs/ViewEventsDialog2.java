package com.gmail.comcorecrew.comcore.dialogs;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.EventLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.classes.modules.Calendar;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.EventEntry;
import com.gmail.comcorecrew.comcore.server.id.CalendarID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ViewEventsDialog2 extends DialogFragment {

    private CustomAdapter2 adapter;
    private List<EventEntry> eventList = new ArrayList<>();
    private Calendar currentCalendar;
    private java.util.Calendar currentDate;
    private List<EventEntry> eventEntries = new ArrayList<>();
    private List<EventEntry> eventEntries1 = new ArrayList<>();

    /**
     * 0 - View events
     * 1 - Delete events
     */
    private int flag;

    public ViewEventsDialog2 (java.util.Calendar currentDate, List<EventEntry> eventEntries) {
        this.currentDate = currentDate;
        this.eventEntries1 = eventEntries;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.dialog_with_title, container, false);

        if (eventEntries1 == null) {

            ServerConnector.getGroups(result -> {
                for (int i = 0; i < result.data.length; i++) {
                    ServerConnector.getModules(result.data[i], result1 -> {
                        for (int j = 0; j < result1.data.length; j++) {
                            if (result1.data[j].id instanceof CalendarID) {
                                ServerConnector.getEvents((CalendarID) result1.data[j].id, result2 -> {
                                    for (int k = 0; k < result2.data.length; k++) {
                                        eventEntries.add(result2.data[k]);
                                    }

                                    RecyclerView rvGroups = (RecyclerView) rootView.findViewById(R.id.dialog_with_title_recycler);
                                    rvGroups.setLayoutManager(new LinearLayoutManager(getActivity()));
                                    adapter = new CustomAdapter2();
                                    rvGroups.setAdapter(adapter);
                                    rvGroups.setItemAnimator(new DefaultItemAnimator());
                                });
                            }
                        }
                    });
                }
            });
        } else {
            for (int i = 0; i < eventEntries1.size(); i++) {
                eventEntries.add(eventEntries1.get(i));
            }

            RecyclerView rvGroups = (RecyclerView) rootView.findViewById(R.id.dialog_with_title_recycler);
            rvGroups.setLayoutManager(new LinearLayoutManager(getActivity()));
            adapter = new CustomAdapter2();
            rvGroups.setAdapter(adapter);
            rvGroups.setItemAnimator(new DefaultItemAnimator());
        }

        return rootView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /**
         * If the "back" button is clicked, close the dialog box
         */
        view.findViewById(R.id.dialog_with_title_back_button).setOnClickListener(clickedView -> {
            this.dismiss();
        });

    }


    /** The CustomAdapter internal class sets up the RecyclerView, which displays
     * the list of calendar events in the GUI
     */
    public class CustomAdapter2 extends RecyclerView.Adapter<CustomAdapter2.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TextView textView;
            private EventEntry currentEventEntry;

            public ViewHolder(View view) {
                super(view);
                view.setOnClickListener(this);
                textView = (TextView) view.findViewById(R.id.label_invite);
            }

            public TextView getTextView() {
                return textView;
            }

            public void setCurrentEventEntry(EventEntry newEntry) {
                this.currentEventEntry = newEntry;
            }

            @Override
            public void onClick(View view) {
                if (flag == 1) {
                    /** Delete event **/
                    currentCalendar.deleteEvent(currentEventEntry.id);
                    dismiss();
                }
            }

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.subtitle_row_item, viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {

            TextView eventDesc = viewHolder.itemView.findViewById(R.id.row_title);
            TextView eventDate = viewHolder.itemView.findViewById(R.id.row_subtitle);

            EventEntry event = eventList.get(position);
            eventDesc.setText(event.description);
            eventDate.setText(event.format(true));

            viewHolder.setCurrentEventEntry(event);
        }

        @Override
        public int getItemCount() {
            return eventEntries.size();
        }
    }

}
