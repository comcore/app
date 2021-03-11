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
import com.gmail.comcorecrew.comcore.dialogs.EmptyTextErrorDialog;
import com.gmail.comcorecrew.comcore.dialogs.MemberEmailDialog;

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

        userNameBox = (EditText) view.findViewById(R.id.editCUName);
        userEmailBox = (EditText) view.findViewById(R.id.editCUEmail);
        userPasswordBox = (EditText) view.findViewById(R.id.editCUPassword);

        view.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(CreateUserFragment.this)
                        .navigate(R.id.action_createUserFragment_to_loginFragment);
            }
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