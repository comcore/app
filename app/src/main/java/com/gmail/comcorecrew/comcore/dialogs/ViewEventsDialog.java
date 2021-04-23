package com.gmail.comcorecrew.comcore.dialogs;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
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
    private Fragment fragment;

    /**
     * 0 - View events
     * 1 - Delete events
     * 2 - Add event to bulletin board
     * 3 - Modify event
     */
    private int flag;

    public ViewEventsDialog (Fragment fragment, Calendar currentCalendar, java.util.Calendar currentDate, int flag) {
        this.currentCalendar = currentCalendar;
        this.currentDate = currentDate;
        this.flag = flag;
        this.fragment = fragment;
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
            title.setText(R.string.modify_event);
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
                    /** Delete event */
                    currentCalendar.deleteEvent(currentEventEntry.id);
                    dismiss();
                }
                else if (flag == 2) {
                    /** Pin event to the bulletin board */
                    currentCalendar.addToBulletin(currentEventEntry.id, true);
                    dismiss();
                }
                else if (flag == 3) {
                    /** Pass the event to be deleted to CreateEventDialog **/
                    new CreateEventDialog(fragment, currentCalendar, currentEventEntry).show(getParentFragmentManager(), null);
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