package com.gmail.comcorecrew.comcore.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.classes.User;
import com.gmail.comcorecrew.comcore.fragments.MainFragment;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.GroupInviteEntry;

import java.util.ArrayList;

public class ViewInvitesDialog extends DialogFragment {
    private RecyclerView recycleView;
    private CustomAdapter adapter;
    private ArrayList<GroupInviteEntry> inviteList;

    public ViewInvitesDialog () {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_invites, container, false);
        inviteList = new ArrayList<>();

        ServerConnector.getInvites(result -> {

            if (result.isSuccess()) {
                for (int i = 0; i < result.data.length; i++) {
                    GroupInviteEntry groupInvite = result.data[i];
                    inviteList.add(groupInvite);
                }

                // Create the RecyclerView
                RecyclerView rvGroups = (RecyclerView) rootView.findViewById(R.id.view_invites_recycler);
                rvGroups.setLayoutManager(new LinearLayoutManager(getActivity()));
                adapter = new CustomAdapter(inviteList);
                rvGroups.setAdapter(adapter);
                rvGroups.setItemAnimator(new DefaultItemAnimator());

                return;
            }
            else if (result.isFailure()) {

                // TODO display error message
                this.dismiss();
                return;
            }
        });

        return rootView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /**
         * If the "back" button is clicked, close the dialog box
         */
        view.findViewById(R.id.view_invites_back_button).setOnClickListener(clickedView -> {
            this.dismiss();
        });

    }


    /** The CustomAdapter internal class sets up the RecyclerView, which displays
     * the list of members in the GUI
     */
    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

        private ArrayList<GroupInviteEntry> inviteList;

        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder).
         */
        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView textView;
            private GroupInviteEntry currentInviteEntry;


            public ViewHolder(View view) {
                super(view);

                textView = (TextView) view.findViewById(R.id.label_invite);
            }

            public TextView getTextView() {
                return textView;
            }

            public void setGroupInviteEntry(GroupInviteEntry newEntry) {
                this.currentInviteEntry = newEntry;
            }

        }



        /**
         * Initialize the dataset of the Adapter.
         */
        public CustomAdapter(ArrayList<GroupInviteEntry> inviteList) {

            this.inviteList = inviteList;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            // Create a new view, which defines the UI of the list item
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.invite_row_item, viewGroup, false);

            return new ViewHolder(view);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {

            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            viewHolder.getTextView().setText(inviteList.get(position).name);

            viewHolder.setGroupInviteEntry(inviteList.get(position));


        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return inviteList.size();
        }
    }

}

