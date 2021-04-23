package com.gmail.comcorecrew.comcore.dialogs;

import android.content.DialogInterface;
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
import com.gmail.comcorecrew.comcore.caching.EventItem;
import com.gmail.comcorecrew.comcore.classes.AppData;
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
     * 2 - Add event to bulletin board
     * 3 - Modify events
     * 4 - Approve events
     */
    private int flag;

    public ViewEventsDialog (Calendar currentCalendar, java.util.Calendar currentDate, int flag) {
        this.currentCalendar = currentCalendar;
        this.currentDate = currentDate;
        this.flag = flag;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        currentCalendar.refresh();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.dialog_with_title, container, false);

        if (flag == 0 && currentDate != null){
            eventList = currentCalendar.getEntriesByDay(currentDate);
        }
        else {
            eventList = currentCalendar.getApproved();
        }


        // Create the RecyclerView
        RecyclerView rvGroups = (RecyclerView) rootView.findViewById(R.id.dialog_with_title_recycler);
        rvGroups.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new CustomAdapter();
        rvGroups.setAdapter(adapter);
        rvGroups.setItemAnimator(new DefaultItemAnimator());


        return rootView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        TextView title = view.findViewById(R.id.label_dialog_with_title);
        if (flag == 0) {
            title.setText(R.string.view_events);
        }
        else if (flag == 1) {
            title.setText(R.string.delete_events);
        }
        else if (flag == 2) {
            title.setText(R.string.pin_event);
        }
        else if (flag == 3) {
            title.setText("Modify Event");
        }
        else if (flag == 4) {
            title.setText("Approve Event");
        }

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
    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private EventEntry currentEventEntry;

            public ViewHolder(View view) {
                super(view);
                view.setOnClickListener(this);

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
                } else if (flag == 2) {
                    /* Pin event to the bulletin board */
                    currentCalendar.addToBulletin(currentEventEntry.id, true);
                    dismiss();
                } else if (flag == 3) {
                    new CreateEventDialog(getParentFragment(), currentCalendar, currentEventEntry)
                            .show(getParentFragmentManager(), null);
                    dismiss();
                } else if (flag == 4) {
                    currentCalendar.approve(currentEventEntry.id);
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
            return eventList.size();
        }
    }

}