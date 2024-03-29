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

public class ViewPendingEventsDialog extends DialogFragment {

    private CustomAdapter adapter;
    private List<EventEntry> unapprovedEventList = new ArrayList<>();
    private Calendar currentCalendar;

    public ViewPendingEventsDialog (Calendar currentCalendar) {
        this.currentCalendar = currentCalendar;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_invites, container, false);

        unapprovedEventList = currentCalendar.getUnapproved();
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
     * the list of pending calendar events in the GUI
     */
    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView textView;
            private EventEntry currentEventEntry;

            public ViewHolder(View view) {
                super(view);

                textView = (TextView) view.findViewById(R.id.label_invite);

                view.findViewById(R.id.accept_invite_button).setOnClickListener(clickedView -> {

                    currentCalendar.approve(currentEventEntry.id);
                    notifyDataSetChanged();
                    dismiss();
                });

                view.findViewById(R.id.reject_invite_button).setOnClickListener(clickedView -> {
                    currentCalendar.disapprove(currentEventEntry.id);
                    notifyDataSetChanged();
                    dismiss();
                });
            }

            public TextView getTextView() {
                return textView;
            }

            public void setCurrentEventEntry(EventEntry newEntry) {
                this.currentEventEntry = newEntry;
            }

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.pending_event_row_item, viewGroup, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {

            TextView eventDesc = viewHolder.itemView.findViewById(R.id.pending_event_desc);
            TextView eventDate = viewHolder.itemView.findViewById(R.id.pending_event_date);

            EventEntry event = unapprovedEventList.get(position);
            eventDesc.setText(event.description);
            eventDate.setText(event.format(true));

            viewHolder.setCurrentEventEntry(event);

        }

        @Override
        public int getItemCount() {
            return unapprovedEventList.size();
        }
    }

}