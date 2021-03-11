package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.server.id.UserID;

public class GroupFragment extends Fragment {

    /** The id of the group that this fragment displays **/
    private int currentGroupID;

    public GroupFragment() {
        // Required empty public constructor
    }

    public static GroupFragment newInstance() {
        GroupFragment fragment = new GroupFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /** When the view is created, GroupFragment should get a Group using the ID number
         * passed to it by MainFragment. **/
        currentGroupID = GroupFragmentArgs.fromBundle(getArguments()).getGroupID();

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /** Displays the name of the current group
         * TODO
         * Currently displays the groupID, but should be able to access a group
         * object using currentGroupID, and then get the group name from that object**/
        TextView welcomeText = (TextView) view.findViewById(R.id.label_group_fragment);
        welcomeText.setText(Integer.toString(currentGroupID));

    }
}