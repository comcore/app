package com.gmail.comcorecrew.comcore.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.fragments.GroupFragment;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.gmail.comcorecrew.comcore.server.id.UserID;

public class TransferOwnershipDialog extends DialogFragment {
    private final int message;
    private GroupID groupID;
    private GroupRole targetRole;

    public TransferOwnershipDialog(GroupID groupID, GroupRole targetRole, int message) {
        this.message = message;
        this.groupID = groupID;
        this.targetRole = targetRole;
    }


    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        EditText text = new EditText(getContext());
        return new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setView(text)
                .setPositiveButton(R.string.ok, (dialog, id) -> {
                    if (text.getText().toString().isEmpty()) {
                        this.dismiss();
                    }
                    else {
                        UserID userEmail = new UserID(text.getText().toString());
                        ServerConnector.setRole(groupID, userEmail, targetRole, result -> {
                            if (result.isFailure()) {
                                return;
                            }

                            // TODO Show success message
                        });
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }
}