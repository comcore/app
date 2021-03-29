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
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.server.LoginStatus;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.GroupID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateGroupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateGroupFragment extends Fragment {
    private boolean chat = false;
    private boolean list = false;
    private boolean calendar = false;

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

                //TextView text = (TextView) view.findViewById(R.id.label_create_group);
                //text.setText(result.data.toString());
                ServerConnector.createChat(result.data, groupName, result1 -> {
                    if (result1.isFailure()) {
                        new ErrorDialog(R.string.error_cannot_connect)
                                .show(getParentFragmentManager(), null);
                    } else {
                        System.out.println("Chat created!");
                    }
                });
                NavHostFragment.findNavController(this)
                        .popBackStack();
            });

        });

    }
}