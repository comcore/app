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
import com.gmail.comcorecrew.comcore.server.LoginStatus;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.UserID;

public class LoginFragment extends Fragment {

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
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
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // TODO reset password button?

        view.findViewById(R.id.loginButton).setOnClickListener(clickedView -> {
            EditText usernameView = view.findViewById(R.id.editUsername);
            EditText passwordView = view.findViewById(R.id.editPassword);

            String email = usernameView.getText().toString();
            String pass = passwordView.getText().toString();

            ServerConnector.login(email, pass, result -> {
                if (result.isFailure()) {
                    // TODO Handle cannot connect to server
                    return;
                }

                LoginStatus status = result.data;
                switch (status) {
                    case SUCCESS:
                        NavHostFragment.findNavController(LoginFragment.this)
                                .navigate(R.id.action_loginFragment_to_mainFragment);
                        break;
                    case ENTER_CODE:
                        // TODO Confirm email address with code
                        break;
                    case DOES_NOT_EXIST:
                        // TODO Handle account doesn't exist
                        break;
                    case INVALID_PASSWORD:
                        // TODO Handle invalid password
                        break;
                }
            });
        });

        view.findViewById(R.id.createUserButton).setOnClickListener(view1 -> {
            NavHostFragment.findNavController(LoginFragment.this)
                    .navigate(R.id.action_loginFragment_to_createUserFragment);
        });

    }

}