package com.gmail.comcorecrew.comcore.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.caching.UserStorage;
import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.classes.User;
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.gmail.comcorecrew.comcore.server.id.UserID;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateGroupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateGroupFragment extends Fragment {
    private boolean chat = false;
    private boolean list = false;
    private boolean calendar = false;
    private GroupID parentGroupID = null;
    private ArrayList<UserID> members = new ArrayList<UserID>();

    public CreateGroupFragment() {
        // Required empty public constructor
    }

    public static CreateGroupFragment newInstance() {
        CreateGroupFragment fragment = new CreateGroupFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentGroupID = CreateGroupFragmentArgs.fromBundle(getArguments()).getParentGroupID();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_create_group, container, false);

        /** If no parentGroupID was passed to this fragment, then elements specific to creating
         * sub groups should not be shown.
         */
        if (parentGroupID == null) {
            rootView.findViewById(R.id.total_members).setVisibility(View.INVISIBLE);
            rootView.findViewById(R.id.create_group_scroller).setVisibility(View.INVISIBLE);
        }
        else {

            RecyclerView rvGroups = (RecyclerView) rootView.findViewById(R.id.create_group_recycler);
            LinearLayoutManager groupLayout = new LinearLayoutManager(getActivity());
            rvGroups.setLayoutManager(groupLayout);
            CustomAdapter groupAdapter = new CustomAdapter();
            rvGroups.setAdapter(groupAdapter);
            rvGroups.setItemAnimator(new DefaultItemAnimator());
        }
        return rootView;
    }

    private void updateMemberListDisplay() {
        TextView totalMembers = getView().findViewById(R.id.total_members);
        String totalMembersString = "Total Members: " + String.valueOf(members.size());
        totalMembers.setText(totalMembersString);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateMemberListDisplay();

        /**
         * If the "back" button is clicked, return to the main page
         */
        view.findViewById(R.id.create_group_back_button).setOnClickListener(clickedView -> {
            NavHostFragment.findNavController(this)
                    .popBackStack();
        });

        view.findViewById(R.id.switch_chat).setOnClickListener(clickedView -> {
            chat = !chat;
        });

        /**
         * If the "submit" button is clicked, try to create a group using the given information
         */
        view.findViewById(R.id.create_group_submit_button).setOnClickListener(clickedView -> {
            EditText groupNameView = view.findViewById(R.id.editGroupName);
            String groupName = groupNameView.getText().toString();

            if (groupName.isEmpty()) {
                ErrorDialog.show(R.string.error_missing_data);
                return;
            }

            /** If the new group is not a sub group **/
            if (parentGroupID == null) {

                ServerConnector.createGroup(groupName, result -> {
                    if (result.isFailure()) {
                        ErrorDialog.show(R.string.error_cannot_connect);
                        return;
                    }

                    //TextView text = (TextView) view.findViewById(R.id.label_create_group);
                    //text.setText(result.data.toString());
                    ServerConnector.createChat(result.data, groupName, result1 -> {
                        if (result1.isFailure()) {
                            ErrorDialog.show(R.string.error_cannot_connect);
                        }
                    });
                    NavHostFragment.findNavController(this)
                            .popBackStack();
                });
            }
            /** If the new group is a sub group **/
            else {
                /** TODO Create sub group using list of memberIDs
                 * Note that the members ArrayList does not include the current user. The person
                 * creating the group should always be added to the new group **/
            }

        });
    }

    /** The CustomAdapter internal class sets up the RecyclerView, which displays
     * the list of members of the parent group in the GUI
     */
    class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
        private ArrayList<User> nonOwnerUsers;

        public CustomAdapter() {
            nonOwnerUsers = AppData.getGroup(parentGroupID).getUsers();
            nonOwnerUsers.remove(UserStorage.getUser(AppData.self.getID()));
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TextView textView;
            private ImageView viewTag;
            private User currentUser;

            public ViewHolder(View view) {
                super(view);
                view.setOnClickListener(this);
                // Define click listener for the ViewHolder's View

                textView = (TextView) view.findViewById(R.id.label_member);
                viewTag = (ImageView) view.findViewById(R.id.view_member_tag);
            }

            public TextView getTextView() {
                return textView;
            }

            public void setUser(User currentUser) {
                this.currentUser = currentUser;
            }

            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                if (members.contains(currentUser.getID())) {
                    members.remove(currentUser.getID());
                    textView.setTextColor(R.color.black);
                }
                else {
                    members.add(currentUser.getID());
                    textView.setTextColor(R.color.owner_color);
                }
                updateMemberListDisplay();
            }

        }

        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.member_row_item, viewGroup, false);

            return new CustomAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CustomAdapter.ViewHolder viewHolder, final int position) {

            User currentUser = nonOwnerUsers.get(position);
            viewHolder.getTextView().setText(currentUser.getName());
            viewHolder.setUser(currentUser);

            /** The group role tag is not necessary when creating a sub group **/
            viewHolder.viewTag.setVisibility(View.INVISIBLE);

        }

        @Override
        public int getItemCount() {
            return nonOwnerUsers.size();
        }
    }
}