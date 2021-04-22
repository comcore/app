package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.caching.PollItem;
import com.gmail.comcorecrew.comcore.classes.modules.Polling;
import com.gmail.comcorecrew.comcore.dialogs.CreatePollItemDialog;

public class PollingFragment extends Fragment {
    public static Polling polling;
    private CustomAdapter pollingAdapter;

    public PollingFragment() {
        // Required empty public constructor
    }

    public static PollingFragment newInstance() {
        PollingFragment fragment = new PollingFragment();
        return fragment;
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
        View rootView = inflater.inflate(R.layout.fragment_polling, container, false);

        // Create the RecyclerView
        RecyclerView rvGroups = (RecyclerView) rootView.findViewById(R.id.polling_recycler);
        rvGroups.setLayoutManager(new LinearLayoutManager(getActivity()));
        pollingAdapter = new CustomAdapter();
        rvGroups.setAdapter(pollingAdapter);
        rvGroups.setItemAnimator(new DefaultItemAnimator());

        polling.setCallback(this::refresh);
        polling.refresh();

        return rootView;
    }

    public void refresh() {
        pollingAdapter.notifyDataSetChanged();
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /** Displays the name of the current polling module */
        TextView welcomeText = (TextView) view.findViewById(R.id.label_polling_fragment);
        welcomeText.setText(polling.getName());

        /**
         * If the "back" button is clicked, return to the main page
         */
        view.findViewById(R.id.polling_back_button).setOnClickListener(clickedView -> {
            NavHostFragment.findNavController(this)
                    .popBackStack();
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.pollingmenu, menu);
    }

    /**
     * Handles click events for the option menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {

            case (R.id.create_poll):
                /** Handle creating a poll **/
                new CreatePollItemDialog(polling).show(getParentFragmentManager(), null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * The PollingFragment uses the same style of RecyclerView that the MainFragment does to display
     * its list of groups.
     *
     * The CustomAdapter internal class sets up the RecyclerView, which displays
     * the list of polls in the GUI
     */
    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
        private PollItem pollItem;

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            public ViewHolder(View view) {
                super(view);
                view.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                PollItemFragment.currentPoll = pollItem;
                PollItemFragment.parentPolling = polling;
                NavHostFragment.findNavController(PollingFragment.this)
                        .navigate(R.id.action_pollingFragment_to_pollItemFragment);
            }
        }

        public void setPollItem(PollItem currentPollItem) {
            this.pollItem = currentPollItem;
        }

        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.title_row_item, viewGroup, false);

            return new CustomAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CustomAdapter.ViewHolder viewHolder, final int position) {
            TextView titleText = viewHolder.itemView.findViewById(R.id.title_row_text);
            PollItem poll = polling.getPolls().get(position);

            titleText.setText(poll.getDescription());

            setPollItem(polling.getPolls().get(position));

        }

        @Override
        public int getItemCount() {
            return polling.getPolls().size();
        }
    }
}