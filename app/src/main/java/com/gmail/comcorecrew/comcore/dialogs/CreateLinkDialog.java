package com.gmail.comcorecrew.comcore.dialogs;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.GroupID;

public class CreateLinkDialog extends DialogFragment {
    private final Fragment fragment;
    private final GroupID group;

    public CreateLinkDialog(Fragment fragment, GroupID group) {
        this.fragment = fragment;
        this.group = group;
    }

    private void finishLink(Fragment fragment, long expireTimestamp) {
        ServerConnector.createInviteLink(group, expireTimestamp, result -> {
            if (result.isFailure()) {
                ErrorDialog.show(R.string.error_cannot_connect);
                return;
            }

            // Create the link by adding HTTPS
            String link = "https://" + result.data;

            // Copy the link to the clipboard
            Context context = fragment.getContext();
            ClipboardManager clipboard = (ClipboardManager)
                    context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText(null, link));

            // Show the link in a dialog
            ErrorDialog.show("Link copied to clipboard:\n" + link);
        });
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(R.string.create_link_does_expire)
                .setPositiveButton(R.string.yes, (dialog, id) ->
                    new PickDateTimeDialog(fragment, this::finishLink, false)
                            .show(fragment.getParentFragmentManager(), null))
                .setNegativeButton(R.string.no, (dialog, id) ->
                        finishLink(fragment, 0))
                .setNeutralButton(R.string.cancel, null)
                .create();
    }
}