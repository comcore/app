package com.gmail.comcorecrew.comcore.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.gmail.comcorecrew.comcore.R;

public class ErrorDialog extends DialogFragment {
    private final int message;

    public ErrorDialog(int message) {
        this.message = message;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setNegativeButton(R.string.ok, null)
                .create();
    }
}
