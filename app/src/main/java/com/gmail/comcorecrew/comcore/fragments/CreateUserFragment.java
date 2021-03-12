package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.gmail.comcorecrew.comcore.R;

import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.server.ServerConnector;


public class CreateUserFragment extends Fragment {

    public CreateUserFragment() {
        // Required empty public constructor
    }

    public static CreateUserFragment newInstance(String param1, String param2) {
        CreateUserFragment fragment = new CreateUserFragment();
        Bundle args = new Bundle();
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
        return inflater.inflate(R.layout.fragment_create_user, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.cancelButton).setOnClickListener(view1 -> {
            NavHostFragment.findNavController(CreateUserFragment.this)
                    .navigate(R.id.action_createUserFragment_to_loginFragment);

        });

        view.findViewById(R.id.submitButton).setOnClickListener(clickedView -> {
            EditText nameView = view.findViewById(R.id.editCUName);
            EditText emailView = view.findViewById(R.id.editCUEmail);
            EditText passwordView = view.findViewById(R.id.editCUPassword);

            String name = nameView.getText().toString();
            String email = emailView.getText().toString();
            String pass = passwordView.getText().toString();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                new ErrorDialog(R.string.error_missing_data)
                        .show(getParentFragmentManager(), null);
                return;
            }

            ServerConnector.createAccount(name, email, pass, result -> {
                if (result.isFailure()) {
                    new ErrorDialog(R.string.error_cannot_connect)
                            .show(getParentFragmentManager(), null);
                    return;
                }

                boolean created = result.data;
                if (created) {
                    // TODO Confirm email address with code
                } else {
                    new ErrorDialog(R.string.error_already_exists)
                            .show(getParentFragmentManager(), null);
                }
            });
        });
    }
}