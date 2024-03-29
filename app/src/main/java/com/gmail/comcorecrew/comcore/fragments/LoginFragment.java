package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.dialogs.EnterCodeDialog;
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.dialogs.InvalidPasswordDialog;
import com.gmail.comcorecrew.comcore.server.LoginStatus;
import com.gmail.comcorecrew.comcore.server.ServerConnector;

import java.lang.ref.WeakReference;

public class LoginFragment extends Fragment {
    public static boolean alreadyLoggedIn = false;

    private static WeakReference<NavController> navControllerWeakReference;
    private static boolean cancelLogin;

    public static void onLoggedOut() {
        if (navControllerWeakReference == null) {
            cancelLogin = true;
            return;
        }

        navigateBackTo(R.id.loginFragment);
    }

    public static void navigateBackTo(int id) {
        NavController navController = navControllerWeakReference.get();
        if (navController == null) {
            return;
        }

        navController.popBackStack(id, false);
    }

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance() {
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
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController controller = NavHostFragment.findNavController(this);
        navControllerWeakReference = new WeakReference<>(controller);

        // If already logged in, navigate directly to the main view of the app
        if (alreadyLoggedIn) {
            alreadyLoggedIn = false;
            if (!cancelLogin) {
                controller.navigate(R.id.action_loginFragment_to_mainFragment);
            }
            return;
        }

        view.findViewById(R.id.loginButton).setOnClickListener(clickedView -> {
            EditText usernameView = view.findViewById(R.id.editUsername);
            EditText passwordView = view.findViewById(R.id.editPassword);

            String email = usernameView.getText().toString();
            String pass = passwordView.getText().toString();

            if (email.isEmpty()) {
                ErrorDialog.show(R.string.error_missing_data);
                return;
            }

            // Attempt to log into the server
            passwordView.setText("");
            ServerConnector.login(email, pass, result -> {
                if (result.isFailure()) {
                    ErrorDialog.show(R.string.error_cannot_connect);
                    return;
                }

                LoginStatus status = result.data;
                switch (status) {
                    case SUCCESS:
                        // Navigate to the main fragment
                        NavHostFragment.findNavController(LoginFragment.this)
                                .navigate(R.id.action_loginFragment_to_mainFragment);
                        break;
                    case ENTER_CODE:
                        // Show the confirmation code dialog
                        new EnterCodeDialog(this,
                                R.id.action_loginFragment_to_mainFragment
                        ).show(getParentFragmentManager(), null);
                        break;
                    case DOES_NOT_EXIST:
                        ErrorDialog.show(R.string.error_does_not_exist);
                        break;
                    case INVALID_PASSWORD:
                        // Show a dialog with an option to reset the password
                        new InvalidPasswordDialog(this, email)
                                .show(getParentFragmentManager(), null);
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