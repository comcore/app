package com.gmail.comcorecrew.comcore.fragments;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

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

public class ChatFragment extends Fragment {
    public static ChatID chatID;
    private ChatArrayAdapter chatArrayAdapter;
    private ListView listView;
    private EditText chatText;
    private Button buttonSend;
    private Date time;
    private Toolbar mToolbar;
    private boolean side = false;
    private GroupEntry[] group;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        UserEntry user = ServerConnector.getUser();
        ServerConnector.getGroups(result -> {
            if (result.isFailure()) {

                return;
            }
            group = result.data;

        });


        InitializeFields(view);

        chatArrayAdapter = new ChatArrayAdapter(getContext(), R.layout.rightside);
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
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void InitializeFields(View view) {
//        mToolbar = (Toolbar) view.findViewById(R.id.group_chat_bar_layout);
//        setSupportActionBar(mToolbar);
//        getSupportActionBar().setTitle("Group Name");

        buttonSend = (Button) view.findViewById(R.id.send);
        listView = (ListView) view.findViewById(R.id.msgview);
        chatText = (EditText) view.findViewById(R.id.msg);
    }

    private boolean sendChatMessage() {
        ServerConnector.sendMessage(chatID, chatText.getText().toString(), null);
        chatArrayAdapter.add(new ChatMessage(side, chatText.getText().toString(), System.currentTimeMillis()));
        chatText.setText("");
        side = !side;

        return true;
    }
}