package com.gmail.comcorecrew.comcore.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.classes.modules.BulletinBoard;
import com.gmail.comcorecrew.comcore.classes.modules.Calendar;
import com.gmail.comcorecrew.comcore.classes.modules.DummyButton;
import com.gmail.comcorecrew.comcore.classes.modules.Messaging;
import com.gmail.comcorecrew.comcore.classes.modules.PinnedMessages;
import com.gmail.comcorecrew.comcore.classes.modules.Polling;
import com.gmail.comcorecrew.comcore.classes.modules.TaskList;
import com.gmail.comcorecrew.comcore.fragments.GroupFragment;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.ChatID;
import com.gmail.comcorecrew.comcore.server.id.GroupID;

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


        /*
          If the "back" button is clicked, close the dialog box
         */
        view.findViewById(R.id.create_module_back_button).setOnClickListener(clickedView -> {
            this.dismiss();
        });

        /*
          If the "submit" button is clicked, try to create the new module
         */
        view.findViewById(R.id.create_module_submit_button).setOnClickListener(clickedView -> {

            RadioButton chatRadio = view.findViewById(R.id.create_module_chat_radio);
            RadioButton tasklistRadio = view.findViewById(R.id.create_module_tasklist_radio);
            RadioButton pinnedRadio = view.findViewById(R.id.create_module_pinned_chat_radio);
            RadioButton calendarRadio = view.findViewById(R.id.create_module_calendar_radio);
            RadioButton pollingRadio = view.findViewById(R.id.create_module_polling_radio);
            RadioButton bulletinRadio = view.findViewById(R.id.create_module_bulletin_radio);
            RadioButton dummyRadio = view.findViewById(R.id.create_module_dummy_radio);
            EditText moduleName = view.findViewById(R.id.create_module_name_edit);

            if (chatRadio.isChecked()) {
                /* Try to create a chat module */
                new Messaging(moduleName.getText().toString(), AppData.getGroup(groupID));
                this.dismiss();
                fragment.refresh();

            }
            else if (tasklistRadio.isChecked()) {

                new TaskList(moduleName.getText().toString(), AppData.getGroup(groupID));

                this.dismiss();
                fragment.refresh();
            }
            else if (false) { //TODO use pinnedRadio.isChecked() once following line is fixed
                ChatID selectedChat = null; //TODO Prompt user for chat to create module for

                if (selectedChat != null) {
                    new PinnedMessages(moduleName.getText().toString(), AppData.getGroup(groupID),
                            selectedChat);
                }

                this.dismiss();
                fragment.refresh();
            }
            else if (calendarRadio.isChecked()) {
                ServerConnector.createCalendar(groupID, moduleName.getText().toString(), result -> {
                    if (result.isFailure()) {
                        ErrorDialog.show(R.string.error_cannot_connect);
                        return;
                    }

                    this.dismiss();
                    fragment.refresh();
                });

                new Calendar(moduleName.getText().toString(), AppData.getGroup(groupID));

                this.dismiss();
                fragment.refresh();
            }
            else if (pollingRadio.isChecked()) {

                new Polling(moduleName.getText().toString(), AppData.getGroup(groupID));

                this.dismiss();
                fragment.refresh();
            }
            else if (bulletinRadio.isChecked()) {
                new BulletinBoard(moduleName.getText().toString(), AppData.getGroup(groupID));

                this.dismiss();
                fragment.refresh();
            }
            else if (dummyRadio.isChecked()) {
                new DummyButton(moduleName.getText().toString(), AppData.getGroup(groupID));

                this.dismiss();
                fragment.refresh();
            }
            else {
                /* Throw an error if for some reason no radio button is checked */
                ErrorDialog.show(R.string.error_module_not_selected);
            }

        });

    }
}
