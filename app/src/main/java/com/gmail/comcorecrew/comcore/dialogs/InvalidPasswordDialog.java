package com.gmail.comcorecrew.comcore.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.server.ServerConnector;

public class InvalidPasswordDialog extends DialogFragment {
    private final Fragment fragment;
    private final String email;

    public InvalidPasswordDialog(Fragment fragment, String email) {
        this.fragment = fragment;
        this.email = email;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(R.string.error_invalid_password)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.reset_password, (dialog, id) -> {
                    // Start resetting the password
                    ServerConnector.requestReset(email, result -> {
                        if (result.isFailure()) {
                            ErrorDialog.show(R.string.error_cannot_connect);
                            return;
                        }

                        boolean sent = result.data;
                        if (!sent) {
                            ErrorDialog.show(R.string.error_does_not_exist);
                            return;
                        }

                        // Show the confirmation code dialog
                        new EnterCodeDialog(fragment, () -> {
                            // Show a dialog for the user to pick a new password
                            new ResetPasswordDialog(fragment, R.string.pick_new_password)
                                    .show(fragment.getParentFragmentManager(), null);
                        }).show(fragment.getParentFragmentManager(), null);
                    });
                })
                .create();
    }
}