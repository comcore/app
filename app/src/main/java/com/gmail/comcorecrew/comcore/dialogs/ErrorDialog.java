package com.gmail.comcorecrew.comcore.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.gmail.comcorecrew.comcore.R;

/**
 * Utility class to create basic error dialogs.
 */
public class ErrorDialog extends DialogFragment {
    public static FragmentManager fragmentManager;

    /**
     * Show an error dialog with a message from a resource.
     *
     * @param message the message to show
     */
    public static void show(int message) {
        new ErrorDialog(message)
                .show(fragmentManager, null);
    }

    /**
     * Show an error dialog with a message string. The first letter of the string will be
     * automatically capitalized, so this can be used with ServerResult.errorMessage as well.
     *
     * @param message the message to show
     */
    public static void show(String message) {
        // Uppercase the first character of the message
        char firstChar = message.charAt(0);
        if (Character.isLowerCase(firstChar)) {
            message = Character.toUpperCase(firstChar) + message.substring(1);
        }

        new StringErrorDialog(message)
                .show(fragmentManager, null);
    }

    private final int message;

    private ErrorDialog(int message) {
        this.message = message;
    }

    public static class StringErrorDialog extends DialogFragment {
        private final String message;

        private StringErrorDialog(String message) {
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

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .create();
    }
}