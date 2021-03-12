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
import com.gmail.comcorecrew.comcore.server.ServerConnector;

public class EnterCodeDialog extends DialogFragment {
    private final Fragment fragment;
    private final int success;
    private final SuccessCallback callback;

    private final int message;

    public interface SuccessCallback {
        void onSuccess();
    }

    public EnterCodeDialog(Fragment fragment, SuccessCallback callback) {
        this(fragment, 0, callback, R.string.confirm_email);
    }

    public EnterCodeDialog(Fragment fragment, int success) {
        this(fragment, success, null, R.string.confirm_email);
    }

    private EnterCodeDialog(Fragment fragment,
                            int success,
                            SuccessCallback callback,
                            int message) {
        this.fragment = fragment;
        this.success = success;
        this.callback = callback;
        this.message = message;
    }

    private void repeat(int message) {
        new EnterCodeDialog(fragment, success, callback, message)
                .show(fragment.getParentFragmentManager(), null);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        EditText text = new EditText(getContext());
        text.setInputType(InputType.TYPE_CLASS_NUMBER);
        return new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setView(text)
                .setPositiveButton(R.string.ok, (dialog, id) -> {
                    String code = text.getText().toString();
                    if (code.isEmpty()) {
                        repeat(R.string.error_incorrect_code);
                        return;
                    }

                    ServerConnector.enterCode(code, result -> {
                        if (result.isFailure()) {
                            repeat(R.string.error_cannot_connect);
                            return;
                        }

                        boolean correct = result.data;
                        if (correct) {
                            if (callback != null) {
                                callback.onSuccess();
                            }
                            if (success != 0) {
                                NavHostFragment.findNavController(fragment).navigate(success);
                            }
                        } else {
                            repeat(R.string.error_incorrect_code);
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }
}