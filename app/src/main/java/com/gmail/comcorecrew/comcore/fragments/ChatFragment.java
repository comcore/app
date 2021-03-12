package com.gmail.comcorecrew.comcore.fragments;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.fragments.GroupFragment;
import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.classes.User;
import com.gmail.comcorecrew.comcore.classes.modules.ChatArrayAdapter;
import com.gmail.comcorecrew.comcore.classes.modules.ChatMessage;
import com.gmail.comcorecrew.comcore.classes.modules.Messaging;
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.server.ResultHandler;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.ServerResult;
import com.gmail.comcorecrew.comcore.server.entry.GroupEntry;
import com.gmail.comcorecrew.comcore.server.entry.UserEntry;
import com.gmail.comcorecrew.comcore.server.id.ChatID;

import java.util.ArrayList;
import java.util.Date;

public class ChatFragment extends AppCompatActivity {

    private ChatArrayAdapter chatArrayAdapter;
    private ListView listView;
    private EditText chatText;
    private Button buttonSend;
    private Date time;
    private Toolbar mToolbar;
    private boolean side = false;
    private GroupEntry[] group;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_chat);

        ResultHandler<ChatID> handler = null;

        UserEntry user = ServerConnector.getUser();
        ServerConnector.getGroups(result -> {
                    if (result.isFailure()) {

                        return;
                    }
                    group = result.data;

                });

        ServerConnector.createChat(group[0].id, group[0].name + " Chat", handler);



        InitializeFields();

        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.rightside);
        listView.setAdapter(chatArrayAdapter);

        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setAdapter(chatArrayAdapter);

        chatText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return sendChatMessage();
                }
                return false;
            }
        });
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendChatMessage();
            }
        });

        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });
    }

    private void InitializeFields() {
        mToolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Group Name");

        buttonSend = (Button) findViewById(R.id.send);
        listView = (ListView) findViewById(R.id.msgview);
        chatText = (EditText) findViewById(R.id.msg);
    }

    private boolean sendChatMessage() {
        chatArrayAdapter.add(new ChatMessage(side, chatText.getText().toString(), time.getTime()));
        chatText.setText("");
        side = !side;

        /*
        Send message info to server.
         */
        return true;
    }
}