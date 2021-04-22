package com.gmail.comcorecrew.comcore.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.classes.User;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.fragments.MainFragment;

public class SelectMemberDialog extends DialogFragment {
    private Group group;

    public SelectMemberDialog(Group group) {
        this.group = group;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.dialog_with_title, container, false);

        // Create the RecyclerView
        RecyclerView rvGroups = (RecyclerView) rootView.findViewById(R.id.dialog_with_title_recycler);
        rvGroups.setLayoutManager(new LinearLayoutManager(getActivity()));
        SelectMemberDialog.CustomAdapter groupAdapter = new CustomAdapter();
        rvGroups.setAdapter(groupAdapter);
        rvGroups.setItemAnimator(new DefaultItemAnimator());

        return rootView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView labelViewMembers = view.findViewById(R.id.label_dialog_with_title);

        labelViewMembers.setText(R.string.select_member);

        /*
          If the "back" button is clicked, close the dialog box
         */
        view.findViewById(R.id.dialog_with_title_back_button).setOnClickListener(clickedView -> {
            this.dismiss();
        });

    }


    /**
     * The CustomAdapter internal class sets up the RecyclerView, which displays
     * the list of members in the GUI
     */
    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder).
         */
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TextView textView;
            private final ImageView memberViewTag;

            private User currentUser;

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

            @Override
            public void onClick(View view) {
                // Don't show menu for the user
                if (currentUser.getID().equals(AppData.self.getID())) {
                    return;
                }

                // Don't show menu for direct messaging groups
                if (group.isDirect()) {
                    return;
                }

                new UserActionDialog(group, currentUser,
                        CustomAdapter.this::refresh,
                        SelectMemberDialog.this::dismiss)
                        .show(ErrorDialog.fragmentManager, null);
            }
        }

        private void refresh() {
            notifyDataSetChanged();
        }

        // Create new views (invoked by the layout manager)
        @Override
        @NonNull
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            // Create a new view, which defines the UI of the list item
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.member_row_item, viewGroup, false);

            return new ViewHolder(view);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {
            User user = group.getUsers().get(position);
            viewHolder.getTextView().setText(user.getName());
            viewHolder.setCurrentUser(user);

            GroupRole role = group.getRole(user.getID());
            MainFragment.setRoleIndicator(viewHolder.memberViewTag, role, group.isDirect());
        }

        @Override
        public int getItemCount() {
            return group.getUsers().size();
        }
    }

}