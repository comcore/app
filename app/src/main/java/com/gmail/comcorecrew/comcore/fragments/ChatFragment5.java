package com.gmail.comcorecrew.comcore.fragments;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.helpers.MessageListAdapter;
import com.gmail.comcorecrew.comcore.classes.modules.Messaging;
import com.gmail.comcorecrew.comcore.dialogs.StringErrorDialog;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;
import com.gmail.comcorecrew.comcore.server.id.ChatID;
import com.gmail.comcorecrew.comcore.server.id.MessageID;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment5#newInstance} factory method to
 * create an instance of this fragment.
 */

public class ChatFragment5 extends Fragment {
    public static Messaging messaging;
    private MessageID messageID;
    private MessageEntry messageEntry;

    private Button sendButton;
    private EditText messageToBeSent;

    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;

    boolean isEditMode = false;
    boolean isDeleteMode = false;
    private MessageEntry editMessage;

    public ChatFragment5() {
        // Required empty public constructor
    }

    public static ChatFragment5 newInstance() {
        ChatFragment5 fragment = new ChatFragment5();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        //       System.out.println("CREATED CHAT FRAG 5");
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
//        if (bundle != null) {
//            bundle.getClassLoader();
//            currentGroup = bundle.getParcelable("currentGroup");
//  //          messaging = bundle.getParcelableArray("messaging");
//
//        }
//        else {
//            new ErrorDialog(R.string.error_unknown)
//                    .show(getParentFragmentManager(), null);
//        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_chat5, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        messaging.refresh();

        initialize(view);

        //       System.out.println("JUST FINISHED INITIALIZING");

        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setStackFromEnd(true);
        mMessageRecycler.setLayoutManager(manager);
        //mMessageRecycler.setHasFixedSize(true);
//        System.out.println("MAKING AN ARRAY ADAPTER I GUESS");
        mMessageAdapter = new MessageListAdapter(getContext(), messaging);
        mMessageRecycler.setAdapter(mMessageAdapter);
        mMessageRecycler.smoothScrollToPosition(mMessageAdapter.getItemCount());


//        messaging.refresh();
//        messageList = messaging.getEntries();

//        registerForContextMenu(mMessageRecycler);

//        manager.setStackFromEnd(true);

//        for (int i = 0; i < messageList.size(); i++) {
//            System.out.println("3. Message # " + i + ": " + messageList.get(i).contents);
//        }

//        System.out.println("AFTER THE RECYCLER SHIT");

        sendButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                       System.out.println("SHOULD BE SENDING A MESSAGE RIGHT ABOUT NOW");
                if (!isEditMode & !isDeleteMode) {
                    sendMessage();
                } else {
                    sendMessage(messageEntry);
                }
            }
        });
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
//        MenuInflater menuInflater = getContext().getMenuInflater();
//        menuInflater.inflate(R.menu.chat_message_menu, menu);
//        menu.setHeaderTitle("Menu");
//        menu.add(0, v.getId(), 0, "Update");
//        menu.add(0, v.getId(), 0, "Edit");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case 121:
                deleteMessage(item);
                return true;
            case 122:
                editMessage(item);
                return true;
            case 123:
                pinMessage(item);
                return true;
            default:
                return true;
        }
    }

    public void initialize(View view) {
//        System.out.println("INSIDE INITIALIZE");
        sendButton = (Button) view.findViewById(R.id.button_gchat_send);
        messageToBeSent = (EditText) view.findViewById(R.id.edit_gchat_message);
//        System.out.println("Before Recylcer");
        mMessageRecycler = (RecyclerView) view.findViewById(R.id.recycler_gchat);
//        System.out.println("After Recycler");
    }

    public void sendMessage() {
        if (messageToBeSent.getText().toString() == null | messageToBeSent.getText().toString().equals("")) {
            return;
        }

        //  System.out.println("Inside sendMessage");
             System.out.println("Going to send that message");
//        for (int i = 0; i < messaging.getEntries().size(); i++) {
//                   System.out.println("11. Message # " + i + ": " + messaging.getEntries().get(i).contents);
//        }

            System.out.println(messageToBeSent.getText().toString());

        ChatID chatID = (ChatID) messaging.getId();
        ServerConnector.sendMessage(chatID, messageToBeSent.getText().toString(), result -> {
            if (result.isFailure()) {
                //          System.out.println("FAILURE OF THE MESSAGE BEING SENT");
                new StringErrorDialog(result.errorMessage)
                        .show(getParentFragmentManager(), null);
            }
//
//            for (int i = 0; i < messaging.getEntries().size(); i++) {
//                        System.out.println("22. Message # " + i + ": " + messaging.getEntries().get(i).contents);
//            }

                System.out.println("Before addMessage(): " + messaging.getEntries().size());

            messaging.onReceiveMessage(result.data);

            //   System.out.println("Before refreshMessages(): " + this.messaging.getEntries().size());


            System.out.println("After refreshMessages(): " + messaging.getEntries().size());
//
//            for (int i = 0; i < messaging.getEntries().size(); i++) {
//                System.out.println("33. Message # " + i + ": " + messaging.getEntries().get(i).contents);
//            }

            mMessageAdapter.notifyDataSetChanged();
            mMessageRecycler.smoothScrollToPosition(mMessageAdapter.getItemCount());

            messageToBeSent.getText().clear();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendMessage(MessageEntry messageEntry1) {
        if (messageToBeSent.getText().toString() == null | messageToBeSent.getText().toString().equals("")) {
            return;
        }
        if (isEditMode) {
            System.out.println("editing");
            //System.out.println(messageToBeSent.getText().toString());
            //  System.out.println("Inside sendMessage");
            MessageEntry messageEntry2 = new MessageEntry(messageEntry1.id, messageEntry1.sender, messageEntry1.timestamp, messageToBeSent.getText().toString());

            ServerConnector.updateMessage(messageEntry1.id, messageToBeSent.getText().toString(), result -> {
                if (result.isFailure()) {
                    System.out.println(result.errorMessage);
                } else {
                    messaging.onMessageUpdated(result.data);

                    mMessageAdapter.notifyDataSetChanged();
                    mMessageRecycler.smoothScrollToPosition(mMessageAdapter.getItemCount());

                    messageToBeSent.getText().clear();
                }
            });

            isEditMode = false;
        } else {
            //  System.out.println("Inside sendMessage");
            messaging.onMessageUpdated(messageEntry1);
            mMessageAdapter.notifyDataSetChanged();
            mMessageRecycler.smoothScrollToPosition(mMessageAdapter.getItemCount());

            messageToBeSent.getText().clear();
            isDeleteMode = false;
        }
    }

        private void deleteMessage (MenuItem item){
            messageEntry = messaging.getEntry(item.getGroupId());

            isDeleteMode = true;

            mMessageAdapter.notifyDataSetChanged();
            mMessageRecycler.smoothScrollToPosition(mMessageAdapter.getItemCount());

            messageToBeSent.getText().clear();
        }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void editMessage(MenuItem item) {
        messageEntry = messaging.getEntry(item.getGroupId());
//        System.out.println("Group id: " + item.getGroupId());
//        System.out.println("Size: " + messaging.getEntries().size());
 //       System.out.println("MessageID w/ item: " + messageID);
 //       int x = messaging.getEntries().size() - item.getGroupId() - 1;
 //       System.out.println("x: " + x);
 //       messageID = messaging.getEntries().get(x).id;
        messageToBeSent.setText(messaging.getEntries().get(item.getGroupId()).contents);

//        for (int i = 0; i < messageList.size(); i++) {
//            System.out.println("Index " + i + ": " + messageList.get(i).id + " " + messageList.get(i).contents);
//        }
        System.out.println("MessageID: " + messageID);
        isEditMode = true;
    }

    private void pinMessage(MenuItem item) {
//        item.getGroupId();
//        MessageID messageID = messageList.get(item.getGroupId()).id;
//        messaging.createPinnedMessages();
//
//        mMessageAdapter.notifyDataSetChanged();
//        mMessageRecycler.smoothScrollToPosition(mMessageAdapter.getItemCount());
//
//        messageToBeSent.getText().clear();
    }
}