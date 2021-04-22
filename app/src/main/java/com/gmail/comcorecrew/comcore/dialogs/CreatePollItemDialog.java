package com.gmail.comcorecrew.comcore.dialogs;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.classes.modules.Calendar;
import com.gmail.comcorecrew.comcore.classes.modules.Polling;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.fragments.PollItemFragment;
import com.gmail.comcorecrew.comcore.server.entry.TaskEntry;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class CreatePollItemDialog extends DialogFragment {

    private Polling polling;
    private ArrayList<String> choices = new ArrayList<>();
    private CustomAdapter adapter;

    public CreatePollItemDialog(Polling polling) {
        this.polling = polling;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_create_poll, container, false);

        // Create the RecyclerView
        RecyclerView rvGroups = (RecyclerView) rootView.findViewById(R.id.create_poll_choices_recycler);
        rvGroups.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new CustomAdapter();
        rvGroups.setAdapter(adapter);
        rvGroups.setItemAnimator(new DefaultItemAnimator());

        return rootView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText desc = view.findViewById(R.id.create_poll_desc);
        EditText optionDesc = view.findViewById(R.id.create_poll_edit_option);

        /**
         If the "cancel" button is clicked, close the dialog box
         */
        view.findViewById(R.id.create_poll_cancel).setOnClickListener(clickedView -> {
            this.dismiss();
        });

        /**
         * If the "create option" button is clicked, add an option
         */
        view.findViewById(R.id.create_poll_create_option).setOnClickListener(clickedView -> {

            if (optionDesc.getText().length() > 0) {
                choices.add(optionDesc.getText().toString());
            }
            optionDesc.getText().clear();
            adapter.notifyDataSetChanged();
        });

        /**
         * If the "submit" button is clicked, create the event
         */
        view.findViewById(R.id.create_poll_submit).setOnClickListener(clickedView -> {
            String[] choiceArray = new String[choices.size()];
            polling.sendPoll(desc.getText().toString(), choices.toArray(choiceArray));
            this.dismiss();
        });

    }

    /** The CustomAdapter internal class sets up the RecyclerView, which displays
     * the list of created choices in the GUI
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
                    .inflate(R.layout.title_row_item, viewGroup, false);

            return new CustomAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CustomAdapter.ViewHolder viewHolder, final int position) {

            TextView titleText = viewHolder.itemView.findViewById(R.id.title_row_text);
            titleText.setText(choices.get(position));

        }

        @Override
        public int getItemCount() {
            return choices.size();
        }
    }
}