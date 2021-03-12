package com.gmail.comcorecrew.comcore.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.server.ServerConnector;

public class ResetPasswordDialog extends DialogFragment {
    private final Fragment fragment;
    private final int message;

    public ResetPasswordDialog(Fragment fragment, int message) {
        this.fragment = fragment;
        this.message = message;
    }

    private void repeat(int message) {
        new ResetPasswordDialog(fragment, message)
                .show(fragment.getParentFragmentManager(), null);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        EditText text = new EditText(getContext());
        text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        return new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setView(text)
                .setPositiveButton(R.string.ok, (dialog, id) -> {
                    String pass = text.getText().toString();
                    if (pass.isEmpty()) {
                        repeat(R.string.error_missing_pass);
                        return;
                    }

                    ServerConnector.finishReset(pass, result -> {
                        if (result.isFailure()) {
                            repeat(R.string.error_cannot_connect);
                            return;
                        }

                        NavHostFragment.findNavController(fragment)
                                .navigate(R.id.action_loginFragment_to_mainFragment);
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }
}