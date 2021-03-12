package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.dialogs.ConfirmEmailDialog;
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.dialogs.MemberEmailDialog;
import com.gmail.comcorecrew.comcore.server.LoginStatus;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.GroupID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateGroupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateGroupFragment extends Fragment {

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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_group, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /** If the "Add from email" button is clicked, allow the user to enter the email of
         * a user they want to add to the new group
         */
        view.findViewById(R.id.m_email_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MemberEmailDialog emailDialog = new MemberEmailDialog();
                emailDialog.show(getParentFragmentManager(), "member_email");
            }
        });

        /**
         * If the "back" button is clicked, return to the main page
         */
        view.findViewById(R.id.create_group_back_button).setOnClickListener(clickedView -> {
            NavHostFragment.findNavController(CreateGroupFragment.this)
                    .navigate(R.id.action_createGroupFragment_to_mainFragment);
        });

        /**
         * If the "submit" button is clicked, try to create a group using the given information
         */
        view.findViewById(R.id.create_group_submit_button).setOnClickListener(clickedView -> {
            EditText groupNameView = view.findViewById(R.id.editGroupName);

            String groupName = groupNameView.getText().toString();

            if (groupName.isEmpty()) {
                new ErrorDialog(R.string.error_missing_data)
                        .show(getParentFragmentManager(), null);
                return;
            }

            ServerConnector.createGroup(groupName, result -> {
                if (result.isFailure()) {
                    new ErrorDialog(R.string.error_cannot_connect)
                            .show(getParentFragmentManager(), null);
                    return;
                }
                else if (result.isSuccess()) {
                    //TextView text = (TextView) view.findViewById(R.id.label_create_group);
                    //text.setText(result.data.toString());
                    NavHostFragment.findNavController(CreateGroupFragment.this)
                            .navigate(R.id.action_createGroupFragment_to_mainFragment);
                    return;
                }

            });

        });

    }
}