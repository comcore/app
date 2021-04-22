package com.gmail.comcorecrew.comcore.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.classes.User;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.fragments.LoginFragment;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.gmail.comcorecrew.comcore.server.id.UserID;

import java.util.ArrayList;

public class UserActionDialog extends DialogFragment {
    private final ArrayList<Runnable> onClick = new ArrayList<>();
    private final ArrayList<String> items = new ArrayList<>();

    public UserActionDialog(Group group, User user, Runnable refresh, Runnable dismiss) {
        GroupID groupId = group.getGroupId();
        UserID userId = user.getID();

        // Add the direct message button
        addAction("Direct Message", () -> {
            ServerConnector.createDirectMessage(userId, result -> {
                if (result.isFailure()) {
                    ErrorDialog.show(result.errorMessage);
                    return;
                }

                // Create a default chat for the group if possible
                ServerConnector.createChat(result.data, "Direct Message", null);

                // Return to the main menu
                LoginFragment.navigateBackTo(R.id.mainFragment);
                dismiss.run();
            });
        });

        // The rest of the actions are moderator only
        GroupRole role = group.getGroupRole();
        if (role == GroupRole.USER) {
            return;
        }

        // Only display these actions if the user can do them
        boolean targetIsUser = group.getRole(userId) == GroupRole.USER;
        if (role == GroupRole.OWNER || targetIsUser) {
            // Add the mute/unmute button
            Boolean muted = group.getUserMuted(userId);
            if (muted != null) {
                addAction(muted ? "Unmute User" : "Mute User", () -> {
                    ServerConnector.setMuted(groupId, userId, !muted, result -> {
                        if (result.isFailure()) {
                            ErrorDialog.show(result.errorMessage);
                            return;
                        }

                        group.setUserMuted(userId, !muted);
                        refresh.run();
                    });
                });
            }

            // Add the kick button
            addAction("Kick from Group", () -> {
                ServerConnector.kick(groupId, userId, result -> {
                    if (result.isFailure()) {
                        ErrorDialog.show(result.errorMessage);
                        return;
                    }

                    group.refreshUsers(null, refresh);
                });
            });

            // Add the add/remove moderator button
            addAction(targetIsUser ? "Add Moderator" : "Remove Moderator", () -> {
                GroupRole newRole = targetIsUser ? GroupRole.MODERATOR : GroupRole.USER;
                ServerConnector.setRole(groupId, userId, newRole, result -> {
                    if (result.isFailure()) {
                        ErrorDialog.show(result.errorMessage);
                        return;
                    }

                    ArrayList<UserID> moderators = group.getModerators();
                    int userIndex = moderators.indexOf(userId);
                    if (targetIsUser) {
                        if (userIndex == -1) {
                            moderators.add(userId);
                        }
                    } else if (userIndex != -1) {
                        moderators.remove(userIndex);
                    }

                    refresh.run();
                });
            });
        }

        // The rest of the actions are owner only
        if (role != GroupRole.OWNER) {
            return;
        }

        // Add the transfer ownership button
        addAction("Transfer Ownership", () -> {
            ServerConnector.setRole(groupId, userId, GroupRole.OWNER, result -> {
                if (result.isFailure()) {
                    ErrorDialog.show(result.errorMessage);
                    return;
                }

                group.setOwner(userId);

                UserID selfId = AppData.self.getID();
                ArrayList<UserID> moderators = group.getModerators();
                if (!moderators.contains(selfId)) {
                    moderators.add(selfId);
                }

                refresh.run();
            });
        });
    }

    private void addAction(String message, Runnable action) {
        items.add(message);
        onClick.add(action);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.select_action)
                .setItems(items.toArray(new String[0]), (dialog, index) -> {
                    onClick.get(index).run();
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }
}