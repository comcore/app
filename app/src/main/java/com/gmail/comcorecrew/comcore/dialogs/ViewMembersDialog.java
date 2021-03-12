package com.gmail.comcorecrew.comcore.dialogs;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.fragments.MainFragment;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.GroupID;

import java.util.ArrayList;

public class ViewMembersDialog extends DialogFragment {
    private RecyclerView recycleView;
    private MainFragment.CustomAdapter adapter;
    private ArrayList<User> userList;
    private GroupID currentGroup;

    /** 0 - View Members
     * 1 - Transfer Ownership
     * 2 - Add Moderator
     * 3 - Remove Moderator
     * 4 - Kick User
     */
    private int flag;

    public ViewMembersDialog (ArrayList<User> userList, GroupID currentGroup, int flag) {
        this.userList = userList;
        this.flag = flag;
        this.currentGroup = currentGroup;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.view_group_members, container, false);

        // Create the RecyclerView
        RecyclerView rvGroups = (RecyclerView) rootView.findViewById(R.id.view_members_recycler);
        rvGroups.setLayoutManager(new LinearLayoutManager(getActivity()));
        ViewMembersDialog.CustomAdapter groupAdapter = new ViewMembersDialog.CustomAdapter(userList, flag);
        rvGroups.setAdapter(groupAdapter);
        rvGroups.setItemAnimator(new DefaultItemAnimator());

        return rootView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView labelViewMembers = view.findViewById(R.id.label_view_members);

        if (flag == 0) {
            labelViewMembers.setText(R.string.view_members);
        }
        else if (flag == 1) {
            labelViewMembers.setText(R.string.pick_transfer_ownership);
        }
        else if (flag == 2) {
            labelViewMembers.setText(R.string.pick_add_moderator);
        }
        else if (flag == 3) {
            labelViewMembers.setText(R.string.pick_remove_moderator);
        }
        else if (flag == 4) {
            labelViewMembers.setText(R.string.pick_kick_user);
        }


        /**
         * If the "back" button is clicked, close the dialog box
         */
        view.findViewById(R.id.view_members_back_button).setOnClickListener(clickedView -> {
            this.dismiss();
        });

    }


    /** The CustomAdapter internal class sets up the RecyclerView, which displays
     * the list of members in the GUI
     */
    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

        private ArrayList<User> userList;
        private int flag;

        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder).
         */
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TextView textView;
            private User currentUser;
            private ImageView memberViewTag;

            public ViewHolder(View view) {
                super(view);
                view.setOnClickListener(this);

                textView = (TextView) view.findViewById(R.id.label_member);
                memberViewTag = (ImageView) view.findViewById(R.id.view_member_tag);
            }

            public TextView getTextView() {
                return textView;
            }

            public void setCurrentUser(User currentUser) {
                this.currentUser = currentUser;
            }

            /** TODO Remove SuppressLint */
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {

                if (flag == 1) {
                    ServerConnector.setRole(currentGroup, currentUser.getID(), GroupRole.OWNER, result -> {
                        if (result.isSuccess()) {

                            view.findViewById(R.id.view_member_tag).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.view_member_tag).setBackgroundColor(R.color.owner_color);


                            // TODO Success Message
                        }
                        else {
                            // TODO Failure Message
                        }
                    });

                }
                else if (flag == 2) {
                    ServerConnector.setRole(currentGroup, currentUser.getID(), GroupRole.MODERATOR, result -> {
                        if (result.isSuccess()) {

                            // TODO Success Message
                            view.findViewById(R.id.view_member_tag).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.view_member_tag).setBackgroundColor(R.color.moderator_color);
                        }
                        else {
                            // TODO Failure Message
                        }
                    });
                }
                else if (flag == 3) {
                    ServerConnector.setRole(currentGroup, currentUser.getID(), GroupRole.USER, result -> {
                        if (result.isSuccess()) {

                            // TODO Success Message
                            view.findViewById(R.id.view_member_tag).setVisibility(View.VISIBLE);

                        }
                        else {
                            // TODO Failure Message
                        }
                    });
                }
                else if (flag == 4) {
                    ServerConnector.kick(currentGroup, currentUser.getID(), result -> {
                        if (result.isSuccess()) {
                            view.findViewById(R.id.view_member_tag).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.view_member_tag).setBackgroundColor(Color.RED);

                        }
                    });
                }
            }

        }

        /**
         * Initialize the dataset of the Adapter.
         */
        public CustomAdapter(ArrayList<User> users, int flag) {

            this.userList = users;
            this.flag = flag;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            // Create a new view, which defines the UI of the list item
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.member_row_item, viewGroup, false);

            return new ViewHolder(view);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {

            viewHolder.getTextView().setText(userList.get(position).getName());
            viewHolder.setCurrentUser(userList.get(position));
            viewHolder.memberViewTag.setVisibility(View.INVISIBLE);

        }

        @Override
        public int getItemCount() {
            return userList.size();
        }
    }

}

