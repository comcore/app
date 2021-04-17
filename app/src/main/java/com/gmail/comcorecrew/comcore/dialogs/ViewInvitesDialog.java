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
import com.gmail.comcorecrew.comcore.fragments.GroupFragment;
import com.gmail.comcorecrew.comcore.fragments.MainFragment;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.GroupInviteEntry;

import java.util.ArrayList;

public class ViewInvitesDialog extends DialogFragment {
    private RecyclerView recycleView;
    private CustomAdapter adapter;
    private ArrayList<GroupInviteEntry> inviteList;
    private MainFragment fragment;

    public ViewInvitesDialog (MainFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_invites, container, false);
        inviteList = new ArrayList<>();

        ServerConnector.getInvites(result -> {
            if (result.isFailure()) {
                // TODO display error message
                this.dismiss();
                return;
            }

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
     * the list of invites in the GUI
     */
    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

        private ArrayList<GroupInviteEntry> inviteList;

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView textView;
            private GroupInviteEntry currentInviteEntry;


            public ViewHolder(View view) {
                super(view);

                textView = (TextView) view.findViewById(R.id.label_invite);

                view.findViewById(R.id.accept_invite_button).setOnClickListener(clickedView -> {
                    ServerConnector.replyToInvite(currentInviteEntry.id, true, result -> {
                        if (result.isFailure()) {
                            ErrorDialog.show(R.string.error_cannot_connect);
                            return;
                        }

                        inviteList.remove(currentInviteEntry);
                        notifyDataSetChanged();
                        fragment.refresh();
                    });
                });

                view.findViewById(R.id.reject_invite_button).setOnClickListener(clickedView -> {
                    ServerConnector.replyToInvite(currentInviteEntry.id, false, result -> {
                        if (result.isFailure()) {
                            ErrorDialog.show(R.string.error_cannot_connect);
                            return;
                        }

                        inviteList.remove(currentInviteEntry);
                        notifyDataSetChanged();
                    });
                });
            }

            public TextView getTextView() {
                return textView;
            }

            public void setGroupInviteEntry(GroupInviteEntry newEntry) {
                this.currentInviteEntry = newEntry;
            }

        }

        public CustomAdapter(ArrayList<GroupInviteEntry> inviteList) {

            this.inviteList = inviteList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.invite_row_item, viewGroup, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {

            viewHolder.getTextView().setText(inviteList.get(position).name);

            viewHolder.setGroupInviteEntry(inviteList.get(position));


        }

        @Override
        public int getItemCount() {
            return inviteList.size();
        }
    }

}

