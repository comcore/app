package com.gmail.comcorecrew.comcore.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.GroupID;

public class SettingsFragment extends Fragment {

    GroupID currentGroupID = null;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentGroupID = SettingsFragmentArgs.fromBundle(getArguments()).getGroupId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        /** The twoFactorSwitch should be toggled depending on whether the user has previously
         * enabled two factor authentication.
         */
        Switch twoFactorSwitch = rootView.findViewById(R.id.settings_two_factor_switch);

        ServerConnector.getTwoFactor(result -> {
            if (result.isSuccess()) {
                if (result.data.booleanValue() == true) {
                    twoFactorSwitch.setChecked(true);
                }
                else {
                    twoFactorSwitch.setChecked(false);
                }
            }
            else {
                new ErrorDialog(R.string.error_cannot_connect)
                        .show(getParentFragmentManager(), null);
                return;
            }
        });


        /** If NO_GROUP was passed to the SettingsFragment, the settings relating to the current
         * group should not be displayed.
         */
        if (currentGroupID.toString().equals("NO_GROUP")) {
            rootView.findViewById(R.id.current_switch_label).setVisibility(View.INVISIBLE);
            rootView.findViewById(R.id.high_current_switch).setVisibility(View.INVISIBLE);
            rootView.findViewById(R.id.medium_current_switch).setVisibility(View.INVISIBLE);
            rootView.findViewById(R.id.low_current_switch).setVisibility(View.INVISIBLE);

        }

        return rootView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /** Handle the back button **/
        view.findViewById(R.id.settings_back_button).setOnClickListener(clickedView -> {
            NavHostFragment.findNavController(this)
                    .popBackStack();
        });

        /** If the user presses submit, update their settings preferences **/
        view.findViewById(R.id.settings_submit_button).setOnClickListener(clickedView -> {

            Switch twoFactorSwitch = view.findViewById(R.id.settings_two_factor_switch);

            /** Set Two Factor Authentication */
            if (twoFactorSwitch.isChecked()) {
                ServerConnector.setTwoFactor(true, result -> {
                    if (result.isFailure()) {
                        new ErrorDialog(R.string.error_cannot_connect)
                                .show(getParentFragmentManager(), null);
                        return;
                    }
                });
            }
            else {
                ServerConnector.setTwoFactor(false, result -> {
                    if (result.isFailure()) {
                        new ErrorDialog(R.string.error_cannot_connect)
                                .show(getParentFragmentManager(), null);
                        return;
                    }
                });
            }

            /** Close the settings fragment **/
            NavHostFragment.findNavController(this)
                    .popBackStack();
        });
    }
}