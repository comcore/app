package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;
import android.text.format.DateFormat;
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
import com.gmail.comcorecrew.comcore.abstracts.CustomModule;
import com.gmail.comcorecrew.comcore.abstracts.Module;
import com.gmail.comcorecrew.comcore.caching.PollItem;
import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.classes.modules.Calendar;
import com.gmail.comcorecrew.comcore.classes.modules.Messaging;
import com.gmail.comcorecrew.comcore.classes.modules.Polling;
import com.gmail.comcorecrew.comcore.classes.modules.TaskList;
import com.gmail.comcorecrew.comcore.dialogs.AddMemberDialog;
import com.gmail.comcorecrew.comcore.dialogs.CreateLinkDialog;
import com.gmail.comcorecrew.comcore.dialogs.CreateModuleDialog;
import com.gmail.comcorecrew.comcore.dialogs.CreateTaskDialog;
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.dialogs.ViewMembersDialog;
import com.gmail.comcorecrew.comcore.dialogs.ViewTasksDialog;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.TaskEntry;
import com.gmail.comcorecrew.comcore.server.id.GroupID;

public class PollItemFragment extends Fragment {
    private PollItem currentPoll;
    private CustomAdapter pollingAdapter;
    private Polling parentPolling;

    public PollItemFragment() {
        // Required empty public constructor
    }

    public static PollItemFragment newInstance() {
        PollItemFragment fragment = new PollItemFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_poll_item, container, false);

        // Create the RecyclerView
        RecyclerView rvGroups = (RecyclerView) rootView.findViewById(R.id.poll_choices_recycler);
        rvGroups.setLayoutManager(new LinearLayoutManager(getActivity()));
        pollingAdapter = new CustomAdapter();
        rvGroups.setAdapter(pollingAdapter);
        rvGroups.setItemAnimator(new DefaultItemAnimator());

        refresh();

        return rootView;
    }

    public void refresh() {
        pollingAdapter.notifyDataSetChanged();
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /** Displays the name of the current group */
        TextView welcomeText = (TextView) view.findViewById(R.id.label_poll_fragment);
        //welcomeText.setText(currentPoll.getName());

        /**
         * If the "back" button is clicked, return to the main page
         */
        view.findViewById(R.id.polling_back_button).setOnClickListener(clickedView -> {
            NavHostFragment.findNavController(this)
                    .popBackStack();
        });
    }


    /**
     * The PollItemFragment uses the same style of RecyclerView that the MainFragment does to display
     * its list of groups.
     *
     * The CustomAdapter internal class sets up the RecyclerView, which displays
     * the list of choices in the GUI
     */
    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(View view) {
                super(view);
            }
        }

        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.poll_choice_row_item, viewGroup, false);

            return new CustomAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CustomAdapter.ViewHolder viewHolder, final int position) {
            TextView titleText = viewHolder.itemView.findViewById(R.id.label_poll_fragment);
            TextView resultText = viewHolder.itemView.findViewById(R.id.results_label);

             /**
             *  PollItem poll = currentPoll.getChoices().get(position);
             *  titleText.setText(poll.getDescription());
              */

        }

        @Override
        public int getItemCount() {
            //return currentPoll.getChoices().size();
            return 0;
        }
    }
}