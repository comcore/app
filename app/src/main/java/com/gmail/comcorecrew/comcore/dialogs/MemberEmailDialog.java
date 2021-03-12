package com.gmail.comcorecrew.comcore.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.gmail.comcorecrew.comcore.R;

public class MemberEmailDialog extends DialogFragment {
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(R.string.add_m_email)
                .setPositiveButton(R.string.confirm, (dialog, id) -> {
                    // Add Member
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    // Cancel
                })
                .create();
    }
}
