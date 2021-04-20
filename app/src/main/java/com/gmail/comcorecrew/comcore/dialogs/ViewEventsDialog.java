package com.gmail.comcorecrew.comcore.dialogs;

import android.os.Bundle;
import android.text.format.DateFormat;
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
import com.gmail.comcorecrew.comcore.server.entry.EventEntry;

import java.util.ArrayList;
import java.util.List;

public class ViewEventsDialog extends DialogFragment {

    private CustomAdapter adapter;
    private List<EventEntry> eventList = new ArrayList<>();
    private Calendar currentCalendar;
    private java.util.Calendar currentDate;

    /**
     * 0 - View events
     * 1 - Delete events
     */
    private int flag;

    public ViewEventsDialog (Calendar currentCalendar, java.util.Calendar currentDate, int flag) {
        this.currentCalendar = currentCalendar;
        this.currentDate = currentDate;
        this.flag = flag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_invites, container, false);

        eventList = currentCalendar.getEntriesByDay(currentDate);

        // Create the RecyclerView
        RecyclerView rvGroups = (RecyclerView) rootView.findViewById(R.id.view_invites_recycler);
        rvGroups.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new CustomAdapter();
        rvGroups.setAdapter(adapter);
        rvGroups.setItemAnimator(new DefaultItemAnimator());


        return rootView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*
          If the "back" button is clicked, close the dialog box
         */
        view.findViewById(R.id.view_invites_back_button).setOnClickListener(clickedView -> {
            this.dismiss();
        });

    }


    /** The CustomAdapter internal class sets up the RecyclerView, which displays
     * the list of calendar events in the GUI
     */
    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

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
                    /* Delete event */
                    currentCalendar.deleteEvent(currentEventEntry.id);
                    dismiss();
                }
            }

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.event_row_item, viewGroup, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {

            TextView eventDesc = viewHolder.itemView.findViewById(R.id.event_description);
            TextView eventDate = viewHolder.itemView.findViewById(R.id.event_date_range);

            eventDesc.setText(eventList.get(position).description);
            String parsedDate = DateFormat.format("MM-dd-yyyy HH:mm", eventList.get(position).start).toString() +
                    " - " + DateFormat.format("MM-dd-yyyy HH:mm", eventList.get(position).end).toString();
            eventDate.setText(parsedDate);

            viewHolder.setCurrentEventEntry(eventList.get(position));

        }

        @Override
        public int getItemCount() {
            return eventList.size();
        }
    }

}