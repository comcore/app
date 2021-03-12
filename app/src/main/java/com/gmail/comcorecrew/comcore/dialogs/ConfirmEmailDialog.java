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

public class ConfirmEmailDialog extends DialogFragment {
    private final Fragment fragment;
    private final int success;
    private final int failure;
    private final int message;

    public ConfirmEmailDialog(Fragment fragment, int success, int failure) {
        this(fragment, success, failure, R.string.confirm_email);
    }

    private ConfirmEmailDialog(Fragment fragment, int success, int failure, int message) {
        this.fragment = fragment;
        this.success = success;
        this.failure = failure;
        this.message = message;
    }

    private void repeat(int message) {
        new ConfirmEmailDialog(fragment, success, failure, message)
                .show(fragment.getParentFragmentManager(), null);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        EditText text = new EditText(getContext());
        text.setInputType(InputType.TYPE_CLASS_NUMBER);
        return new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setView(text)
                .setPositiveButton(R.string.ok, (dialog, id) -> {
                    String code = text.getText().toString();
                    if (code.isEmpty()) {
                        repeat(R.string.error_incorrect_code);
                        return;
                    }

                    ServerConnector.enterCode(code, result -> {
                        if (result.isFailure()) {
                            repeat(R.string.error_cannot_connect);
                        } else if (!result.data) {
                            repeat(R.string.error_incorrect_code);
                        } else if (success != 0) {
                            NavHostFragment.findNavController(fragment).navigate(success);
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    if (failure != 0) {
                        NavHostFragment.findNavController(fragment).navigate(failure);
                    }
                })
                .create();
    }
}