package com.gmail.comcorecrew.comcore.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.gmail.comcorecrew.comcore.R;

public class StringErrorDialog extends DialogFragment {
    private final String message;

    public StringErrorDialog(String message) {
        this.message = message;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .create();
    }
}