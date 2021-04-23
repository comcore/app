package com.gmail.comcorecrew.comcore.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.fragments.MainFragment;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.InviteLinkEntry;

public class InviteLinkDialog extends DialogFragment {
    private static InviteLinkEntry pendingLink;

    private final MainFragment mainFragment;
    private final InviteLinkEntry link;

    private InviteLinkDialog(MainFragment mainFragment, InviteLinkEntry link) {
        this.mainFragment = mainFragment;
        this.link = link;
    }

    public static void setLink(InviteLinkEntry link) {
        pendingLink = link;
    }

    public static void checkExpired() {
        if (pendingLink == null) {
            return;
        }

        if (pendingLink.hasExpired()) {
            InviteLinkDialog.pendingLink = null;
            ErrorDialog.show(R.string.error_link_expired);
        }
    }

    public static void showIfPossible(MainFragment mainFragment) {
        // First make sure the link hasn't expired
        checkExpired();
        if (pendingLink == null) {
            return;
        }

        // Replace the link with null so the dialog isn't displayed twice
        InviteLinkEntry link = pendingLink;
        pendingLink = null;

        // Show the invite dialog with the link
        new InviteLinkDialog(mainFragment, link)
                .show(mainFragment.getParentFragmentManager(), null);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage("Do you want to join " + link.groupName + "?")
                .setPositiveButton(R.string.join, (dialog, id) -> {
                    ServerConnector.useInviteLink(link.inviteLink, result -> {
                        if (result.isFailure()) {
                            ErrorDialog.show(R.string.error_cannot_connect);
                            return;
                        }

                        // The group was joined, so refresh the main fragment accordingly
                        mainFragment.refresh();
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }
}