package com.gmail.comcorecrew.comcore.classes.modules;

import android.annotation.SuppressLint;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.abstracts.CustomModule;
import com.gmail.comcorecrew.comcore.caching.CustomItem;
import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.dialogs.ViewEventsDialog;
import com.gmail.comcorecrew.comcore.server.entry.EventEntry;
import com.gmail.comcorecrew.comcore.server.id.CustomModuleID;

import java.util.ArrayList;
import java.util.List;

public class BulletinBoard extends CustomModule {

    private CustomAdapter adapter;
    private List<EventEntry> eventList = new ArrayList<>();

    public BulletinBoard(String name, CustomModuleID id, Group group) {
        super(name, id, group);
    }

    public BulletinBoard(String name, Group group) {
        super(name, group, "bulletin");
    }

    @Override
    public void viewInit(@NonNull View view, Fragment current) {

        eventList = AppData.getUpcoming(getGroup().getGroupId());

        // Create the RecyclerView
        RecyclerView rvGroups = (RecyclerView) view.findViewById(R.id.simple_recycler);
        rvGroups.setLayoutManager(new LinearLayoutManager(current.getActivity()));
        adapter = new CustomAdapter();
        rvGroups.setAdapter(adapter);
        rvGroups.setItemAnimator(new DefaultItemAnimator());

        TextView welcomeText = (TextView) view.findViewById(R.id.label_simple_fragment);
        welcomeText.setText(R.string.bulletin_board);

        view.findViewById(R.id.simple_back_button).setOnClickListener(clickedView -> {
            NavHostFragment.findNavController(current)
                    .popBackStack();
        });

        refreshView();
    }

    @Override
    public int getLayout() {
        return R.layout.fragment_simple_recycler;
    }

    @Override
    public void refreshView() {
        refresh();
    }

    @Override
    public void refresh() {
        if (adapter == null) {
            adapter = new CustomAdapter();
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void afterCreate() {
        sendMessage("0");
    }


    /** The CustomAdapter internal class sets up the RecyclerView, which displays
     * the list of bulletin board events in the GUI
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
            }

        }

        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.subtitle_row_item, viewGroup, false);

            return new CustomAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CustomAdapter.ViewHolder viewHolder, final int position) {

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
