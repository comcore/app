package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import com.gmail.comcorecrew.comcore.R;

import com.gmail.comcorecrew.comcore.dialogs.EnterCodeDialog;
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
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        view.findViewById(R.id.cancelButton).setOnClickListener(view1 -> {
            NavHostFragment.findNavController(this)
                    .popBackStack();
        });

        view.findViewById(R.id.submitButton).setOnClickListener(clickedView -> {
            EditText nameView = view.findViewById(R.id.editCUName);
            EditText emailView = view.findViewById(R.id.editCUEmail);
            EditText passwordView = view.findViewById(R.id.editCUPassword);

            String name = nameView.getText().toString();
            String email = emailView.getText().toString();
            String pass = passwordView.getText().toString();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                ErrorDialog.show(R.string.error_missing_data);
                return;
            }

            // Start creating an account with the server
            ServerConnector.createAccount(name, email, pass, result -> {
                if (result.isFailure()) {
                    ErrorDialog.show(R.string.error_cannot_connect);
                    return;
                }

                boolean created = result.data;
                if (created) {
                    // Show the confirmation code dialog
                    new EnterCodeDialog(this,
                            R.id.action_createUserFragment_to_mainFragment
                    ).show(getParentFragmentManager(), null);
                } else {
                    ErrorDialog.show(R.string.error_already_exists);
                }
            });
        });
    }
}