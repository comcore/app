package com.gmail.comcorecrew.comcore.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.gmail.comcorecrew.comcore.R;

public class ConfirmEmailDialog extends DialogFragment {
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(R.string.confirm_email)
                .setPositiveButton(R.string.ok, (dialog, id) -> {
                    // Finish logging in
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    // Return to login menu
                })
                .create();
    }
}