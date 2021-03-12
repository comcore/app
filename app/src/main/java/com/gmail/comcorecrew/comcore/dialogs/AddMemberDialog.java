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
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.fragments.GroupFragment;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.gmail.comcorecrew.comcore.server.id.UserID;

public class AddMemberDialog extends DialogFragment {
    private final int message;
    private GroupID groupID;

    public AddMemberDialog(GroupID groupID, int message) {
        this.message = message;
        this.groupID = groupID;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        EditText text = new EditText(getContext());
        text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        return new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setView(text)
                .setPositiveButton(R.string.ok, (dialog, id) -> {
                    if (text.getText().toString().isEmpty()) {
                        this.dismiss();
                    }
                    else {
                        String userEmail = text.getText().toString();
                        ServerConnector.sendInvite(groupID, userEmail, result -> {
                            if (result.isFailure()) {
                                // getParentFragmentManager() throws an error
                                //new ErrorDialog(R.string.error_send_invite).show(getParentFragmentManager(), null);
                                return;
                            }

                            boolean sent = result.data;
                            if (sent) {
                                // getParentFragmentManager() throws an error
                                //new ErrorDialog(R.string.success_send_invite).show(getParentFragmentManager(), null);
                            } else {
                                // getParentFragmentManager() throws an error
                                //new ErrorDialog(R.string.error_does_not_exist)show(getParentFragmentManager(), null);
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }
}