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
import com.gmail.comcorecrew.comcore.abstracts.Module;
import com.gmail.comcorecrew.comcore.caching.GroupStorage;
import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.server.ServerConnector;

import java.util.ArrayList;

public class SettingsFragment extends Fragment {
    public static Group currentGroup = null;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
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
        Switch muteCurrentSwitch = rootView.findViewById(R.id.mute_current_switch);
        Switch mentionCurrentSwitch = rootView.findViewById(R.id.mention_current_switch);

        ServerConnector.getTwoFactor(result -> {
            if (result.isSuccess()) {
                twoFactorSwitch.setChecked(result.data);
            }
        });

        if (currentGroup == null) {
            rootView.findViewById(R.id.current_switch_label).setVisibility(View.INVISIBLE);
            muteCurrentSwitch.setVisibility(View.INVISIBLE);
            mentionCurrentSwitch.setVisibility(View.INVISIBLE);
        } else {
            ArrayList<Module> modules = currentGroup.getModules();
            if (!modules.isEmpty()) {
                Module module = modules.get(0);
                muteCurrentSwitch.setChecked(!module.isMuted());
                mentionCurrentSwitch.setChecked(!module.isMentionMuted());
            }
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
            Switch muteCurrentSwitch = view.findViewById(R.id.mute_current_switch);
            Switch mentionCurrentSwitch = view.findViewById(R.id.mention_current_switch);

            /** Set Two Factor Authentication */
            ServerConnector.setTwoFactor(twoFactorSwitch.isChecked(), result -> {
                if (result.isFailure()) {
                    new ErrorDialog(R.string.error_cannot_connect)
                            .show(getParentFragmentManager(), null);
                }
            });

            if (currentGroup != null) {
                for (Module module : currentGroup.getModules()) {
                    module.setMuted(!muteCurrentSwitch.isChecked());
                    module.setMentionMuted(!mentionCurrentSwitch.isChecked());
                }
            }

            /** Close the settings fragment **/
            NavHostFragment.findNavController(this)
                    .popBackStack();
        });
    }
}