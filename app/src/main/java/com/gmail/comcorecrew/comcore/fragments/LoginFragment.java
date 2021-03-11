package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gmail.comcorecrew.comcore.R;
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

        view.findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /** Here, the user's information should be verified to ensure that they are correct.
                 *
                 * Currently, the app moves to the main page regardless of what information is entered.
                 * A placeholder user name is created here for testing purposes, but the server should
                 * return a UserEntry which is parsed into user information.
                 *
                 * The placeholder string is used to create a UserID in MainFragment
                 */
                String placeholderUserID = "Placeholder User";

                LoginFragmentDirections.ActionLoginFragmentToMainFragment action = LoginFragmentDirections.actionLoginFragmentToMainFragment();
                action.setCurrentUser(placeholderUserID);
                NavHostFragment.findNavController(LoginFragment.this)
                        .navigate(action);
            }
        });

        view.findViewById(R.id.createUserButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(LoginFragment.this)
                        .navigate(R.id.action_loginFragment_to_createUserFragment);
            }
        });

    }

}