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

import com.gmail.comcorecrew.comcore.server.ServerConnector;


public class CreateUserFragment extends Fragment {

    private EditText userNameBox;
    private EditText userPasswordBox;
    private EditText userEmailBox;

    private String userName;
    private String userEmail;
    private String userPassword;

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

        view.findViewById(R.id.submitButton).setOnClickListener(clickedView -> {
            EditText nameView = view.findViewById(R.id.editCUName);
            EditText emailView = view.findViewById(R.id.editCUEmail);
            EditText passwordView = view.findViewById(R.id.editCUPassword);

            String name = nameView.getText().toString();
            String email = emailView.getText().toString();
            String pass = passwordView.getText().toString();

            ServerConnector.createAccount(name, email, pass, result -> {
                if (result.isFailure()) {
                    // TODO Handle cannot connect to server
                    return;
                }

                boolean created = result.data;
                if (created) {
                    // TODO Confirm email address with code
                } else {
                    // TODO Handle account already exists
                }
            });
        });

        view.findViewById(R.id.cancelButton).setOnClickListener(view1 -> {
            NavHostFragment.findNavController(CreateUserFragment.this)
                    .navigate(R.id.action_createUserFragment_to_loginFragment);

        });

        view.findViewById(R.id.submitButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                userName = userNameBox.getText().toString();
                userEmail = userEmailBox.getText().toString();
                userPassword = userPasswordBox.getText().toString();

                if (userName.equals("") || userEmail.equals("") || userPassword.equals("")) {
                    EmptyTextErrorDialog errorDialog = new EmptyTextErrorDialog();
                    errorDialog.show(getParentFragmentManager(), "create_user_error");
                }
            }
        });

    }
}