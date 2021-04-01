package com.gmail.comcorecrew.comcore.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.classes.User;
import com.gmail.comcorecrew.comcore.fragments.GroupFragment;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.GroupID;

import java.util.ArrayList;

public class CreateModuleDialog extends DialogFragment {

    private GroupID groupID;
    private GroupFragment fragment;

    public CreateModuleDialog(GroupID currentGroupId, GroupFragment fragment) {
        this.groupID = currentGroupId;
        this.fragment = fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_create_module, container, false);

        return rootView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        /**
         * If the "back" button is clicked, close the dialog box
         */
        view.findViewById(R.id.create_module_back_button).setOnClickListener(clickedView -> {
            this.dismiss();
        });

        /**
         * If the "submit" button is clicked, try to create the new module
         */
        view.findViewById(R.id.create_module_submit_button).setOnClickListener(clickedView -> {

            RadioButton chatRadio = view.findViewById(R.id.create_module_chat_radio);
            RadioButton tasklistRadio = view.findViewById(R.id.create_module_tasklist_radio);
            EditText moduleName = view.findViewById(R.id.create_module_name_edit);

            if (chatRadio.isChecked()) {
                /** Try to create a chat module **/

                ServerConnector.createChat(groupID, moduleName.getText().toString(), result -> {
                    if (result.isFailure()) {
                        new ErrorDialog(R.string.error_cannot_connect)
                                .show(getParentFragmentManager(), null);
                        return;
                    }

                    this.dismiss();
                    fragment.refresh();
                });
            }
            else if (tasklistRadio.isChecked()) {
                /** Try to create a tasklist module **/
                ServerConnector.createTaskList(groupID, moduleName.getText().toString(), result -> {
                    if (result.isFailure()) {
                        new ErrorDialog(R.string.error_cannot_connect)
                                .show(getParentFragmentManager(), null);
                        return;
                    }

                    this.dismiss();
                    fragment.refresh();
                });
            }
            else {
                /** Throw an error if for some reason no radio button is checked **/
                new ErrorDialog(R.string.error_module_not_selected)
                        .show(getParentFragmentManager(), null);
            }

        });

    }
}
