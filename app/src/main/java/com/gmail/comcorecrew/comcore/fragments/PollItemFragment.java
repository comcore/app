package com.gmail.comcorecrew.comcore.fragments;

import android.annotation.SuppressLint;
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
import com.gmail.comcorecrew.comcore.server.entry.PollOption;
import com.gmail.comcorecrew.comcore.server.entry.TaskEntry;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.gmail.comcorecrew.comcore.server.id.PollID;
import com.gmail.comcorecrew.comcore.server.id.PollListID;

public class PollItemFragment extends Fragment {
    public static PollItem currentPoll;
    private CustomAdapter pollingAdapter;
    public static Polling parentPolling;

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
        welcomeText.setText(currentPoll.getDescription());

        /**
         * If the "back" button is clicked, return to the main page
         */
        view.findViewById(R.id.poll_back_button).setOnClickListener(clickedView -> {
            NavHostFragment.findNavController(this)
                    .popBackStack();
        });

        /**
         * If the "view results" button is clicked, display the number of votes for each choice
         */
        view.findViewById(R.id.view_results_button).setOnClickListener(clickedView -> {
            currentPoll.toggleResultsVisible();
            pollingAdapter.notifyDataSetChanged();
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

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TextView choiceLabel;
            private int currentChoice;

            public ViewHolder(View view) {
                super(view);
                view.setOnClickListener(this);

                choiceLabel = (TextView) view.findViewById(R.id.label_choice);

            }


            public void setCurrentChoice(int newChoice) {
                currentChoice = newChoice;
            }

            @Override
            public void onClick(View v) {
                parentPolling.votePoll(new PollID((PollListID) parentPolling.getId(), currentPoll.getPollId()), currentChoice);
                if (!currentPoll.getResultsVisible()) {
                    currentPoll.toggleResultsVisible();
                }
                parentPolling.refresh();
                refresh();
            }
        }

        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.poll_choice_row_item, viewGroup, false);

            return new CustomAdapter.ViewHolder(view);
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public void onBindViewHolder(CustomAdapter.ViewHolder viewHolder, final int position) {
            TextView titleText = viewHolder.itemView.findViewById(R.id.label_choice);
            TextView resultText = viewHolder.itemView.findViewById(R.id.results_label);

            PollOption option = currentPoll.getOptions().get(position);
            titleText.setText(option.description);

            String resultString;
            if (currentPoll.getTotalVotes() == 0) {
                resultString = "No votes";
            }
            else {
                resultString = String.format("%d votes (%.2f%%)", option.numberOfVotes, 100 * (float) option.numberOfVotes / (float) currentPoll.getTotalVotes());
            }
            resultText.setText(resultString);

            if (currentPoll.getResultsVisible()) {
                resultText.setVisibility(View.VISIBLE);
            }
            else {
                resultText.setVisibility(View.INVISIBLE);
            }

            viewHolder.setCurrentChoice(position);

        }

        @Override
        public int getItemCount() {
            return currentPoll.getOptions().size();
        }
    }
}