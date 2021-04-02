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
import com.gmail.comcorecrew.comcore.caching.GroupStorage;
import com.gmail.comcorecrew.comcore.classes.AppData;
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
        Switch mentionCurrentSwitch = rootView.findViewById(R.id.mention_current_switch);

        ServerConnector.getTwoFactor(result -> {
            if (result.isSuccess()) {
                twoFactorSwitch.setChecked(result.data);
            }
        });

        /** If NO_GROUP was passed to the SettingsFragment, the settings relating to the current
         * group should not be displayed.
         */
        if (currentGroupID.toString().equals("NO_GROUP")) {
            rootView.findViewById(R.id.current_switch_label).setVisibility(View.INVISIBLE);
            rootView.findViewById(R.id.mention_current_switch).setVisibility(View.INVISIBLE);
            rootView.findViewById(R.id.mute_current_switch).setVisibility(View.INVISIBLE);
        }
        else if (!GroupStorage.getGroup(currentGroupID).getModule(0).isMentionMuted()) {
            mentionCurrentSwitch.setChecked(true);

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
            Switch muteAllSwitch = view.findViewById(R.id.mute_all_switch);
            Switch mentionAllSwitch = view.findViewById(R.id.mention_all_switch);
            Switch muteCurrentSwitch = view.findViewById(R.id.mute_current_switch);
            Switch mentionCurrentSwitch = view.findViewById(R.id.mention_current_switch);

            /** Set Two Factor Authentication */
            ServerConnector.setTwoFactor(twoFactorSwitch.isChecked(), result -> {
                if (result.isFailure()) {
                    new ErrorDialog(R.string.error_cannot_connect)
                            .show(getParentFragmentManager(), null);
                }
            });

            /** Check if notifications for the current group should be muted **/
            if (!muteCurrentSwitch.isChecked()) {
                for (int i = 0; i < GroupStorage.getGroup(currentGroupID).getModules().size(); i++) {
                    GroupStorage.getGroup(currentGroupID).getModule(i).setMuted(true);
                }
            }
            else {
                for (int i = 0; i < GroupStorage.getGroup(currentGroupID).getModules().size(); i++) {
                    GroupStorage.getGroup(currentGroupID).getModule(i).setMuted(false);
                }
            }

            /** Check if mention notifications for the current group should be muted **/
            if (!mentionCurrentSwitch.isChecked()) {
                for (int i = 0; i < GroupStorage.getGroup(currentGroupID).getModules().size(); i++) {
                    GroupStorage.getGroup(currentGroupID).getModule(i).setMentionMuted(true);
                }
            }
            else {
                for (int i = 0; i < GroupStorage.getGroup(currentGroupID).getModules().size(); i++) {
                    GroupStorage.getGroup(currentGroupID).getModule(i).setMentionMuted(false);
                }
            }

            /** Check if mention notifications for all groups should be muted **/
            if (!mentionAllSwitch.isChecked()) {
                for (int i = 0; i < AppData.groups.size(); i++) {
                    for (int j = 0; j < AppData.getGroup(i).getModules().size(); j++) {
                        AppData.getGroup(i).getModule(j).setMentionMuted(true);
                    }
                }
            }
            else {
                for (int i = 0; i < AppData.groups.size(); i++) {
                    for (int j = 0; j < AppData.getGroup(i).getModules().size(); j++) {
                        AppData.getGroup(i).getModule(j).setMentionMuted(false);
                    }
                }
            }

            /** Check if mention notifications for all groups should be muted **/
            if (!muteAllSwitch.isChecked()) {
                for (int i = 0; i < AppData.groups.size(); i++) {
                    for (int j = 0; j < AppData.getGroup(i).getModules().size(); j++) {
                        AppData.getGroup(i).getModule(j).setMuted(true);
                    }
                }
            }
            else {
                for (int i = 0; i < AppData.groups.size(); i++) {
                    for (int j = 0; j < AppData.getGroup(i).getModules().size(); j++) {
                        AppData.getGroup(i).getModule(j).setMuted(false);
                    }
                }
            }



            /** Close the settings fragment **/
            NavHostFragment.findNavController(this)
                    .popBackStack();
        });
    }
}