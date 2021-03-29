package com.gmail.comcorecrew.comcore.fragments;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.caching.UserStorage;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.classes.User;
import com.gmail.comcorecrew.comcore.classes.modules.MessageListAdapter;
import com.gmail.comcorecrew.comcore.classes.modules.Messaging;
import com.gmail.comcorecrew.comcore.classes.modules.UserMessage;
import com.gmail.comcorecrew.comcore.dialogs.StringErrorDialog;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;
import com.gmail.comcorecrew.comcore.server.id.ChatID;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.gmail.comcorecrew.comcore.server.id.MessageID;
import com.gmail.comcorecrew.comcore.server.info.UserInfo;
import com.google.gson.internal.bind.util.ISO8601Utils;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment5#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment5 extends Fragment {
    private int chatPosition;
    public static ChatID chatID;
    public static Group currentGroup;
    private Messaging messaging;
    private ArrayList<MessageEntry> messageEntryArrayList = new ArrayList<MessageEntry>(3);
    private MessageEntry[] messageEntries;


    private Button sendButton;
    private EditText messageToBeSent;

    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;

    public ChatFragment5() {
        // Required empty public constructor
    }

    public static ChatFragment5 newInstance() {
        ChatFragment5 fragment = new ChatFragment5();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        Messaging messaging = new Messaging(ServerConnector.getUser().name, chatID, currentGroup);
 //       System.out.println("CREATED CHAT FRAG 5");
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

//        try {
//            UserStorage.init();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        // Inflate the layout for this fragment
 //       System.out.println("DISPLAY CHAT FRAG 5 with id: " + chatID);

//        View viewHolder= LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.item, null, false);
//        viewHolder.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
//        return new ViewOffersHolder(viewHolder);

        return inflater.inflate(R.layout.fragment_chat5, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
 //       System.out.println("ENTERING OnViewCreated IN CHATFRAG5");

 //       System.out.println("ChatID: " + chatID);

  //      System.out.println(ServerConnector.getUser().id);

        MessageID messageID= new MessageID(chatID, chatPosition + 1);

        UserMessage userMessage = new UserMessage(messageID, ServerConnector.getUser(), Instant.now().getEpochSecond(), "FIX ME PLEASE");

//        System.out.println(messageID + " " + ServerConnector.getUser().id + " " + ServerConnector.getUser().name + " " + Instant.now().getEpochSecond() + " " + userMessage.getMessage());

//        System.out.println(userMessage.getMessageID() + " " + userMessage.getUserInfo() + " " + userMessage.getTime2() + " " + "FIX ME PLEASE");

//        MessageEntry messageEntry = new MessageEntry(new MessageID(chatID, chatPosition + 1), new UserInfo(ServerConnector.getUser().id, ServerConnector.getUser().name), userMessage.getTime2(), "FIX ME PLEASE");

 //       System.out.println("MESSAGE ENTRY STATS: " + messageEntry.contents + " " + messageEntry.id + " " + messageEntry.sender + " " + messageEntry.timestamp);

//        MessageEntry messageEntry2 = new MessageEntry(new MessageID(chatID, chatPosition + 2), new UserInfo(ServerConnector.getUser().id, ServerConnector.getUser().name), userMessage.getTime2(), "Is this it??");

//        int x = UserStorage.getInternalId(messageEntry.sender.id);
 //       System.out.println(x);

//        try {
//            UserStorage.addUser(new User(ServerConnector.getUser().id, ServerConnector.getUser().name));
//        } catch (IOException e) {
//            System.out.println("damn we fucked up again");
//        }

//        System.out.println(ServerConnector.getUser().id);
//        System.out.println(UserStorage.getInternalId(ServerConnector.getUser().id));

       // messaging.addMessage(messageEntry);

        final MessageID[] z = new MessageID[1];

//        if (messageEntry == null) {
 //           System.out.println("WHY THE FUCK");
//        } else if (messageEntry != null) {
//            System.out.println("ADDING MESSAGE ENTRY");
//            ServerConnector.sendMessage(chatID, messageEntry.contents, result -> {
//                if (result.isFailure()) {
 //                   System.out.println("FAILED");
//                } else {
//                    z[0] = result.data;
 //                   System.out.println(z[0].module);
 //                   System.out.println(z[0].id);
//                }
//            });
//            messageEntryArrayList.add(messageEntry);
//            messageEntryArrayList.add(messageEntry2);
 //           System.out.println("JUST ADDED MESSAGE ENTRY");
//        }



        ServerConnector.getMessages(chatID, null, null, result -> {
 //           System.out.println("INSIDE THE FIRST GET MESSAGES");
            if (result.isFailure()) {
 //               System.out.println("YOU FAILED GETTING THE MESSAGES IN CHAT FRAG5");
                return;
            } else if (result.data == null) {

 //               System.out.println("Result.data == null");

                } else if (Arrays.deepToString(result.data).equals("[]")){

 //               System.out.println(Arrays.deepToString(result.data));

                } else {
 //               System.out.println("UPDATING ARRAYLIST");
                messageEntryArrayList.addAll(Arrays.asList(result.data));
            }
        });

 //       System.out.println("OUTSIDE OF THE GET MESSAGES PAGE");

        initialize(view);

 //       System.out.println("JUST FINISHED INITIALIZING");

        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        mMessageRecycler.setLayoutManager(manager);
        mMessageRecycler.setHasFixedSize(true);
//        System.out.println("MAKING AN ARRAY ADAPTER I GUESS");
        mMessageAdapter = new MessageListAdapter(getActivity(), messageEntryArrayList);
        mMessageRecycler.setAdapter(mMessageAdapter);

//        System.out.println("AFTER THE RECYCLER SHIT");

        sendButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
 //               System.out.println("SHOULD BE SENDING A MESSAGE RIGHT ABOUT NOW");
                sendMessage();
            }
        });
    }

    public void initialize(View view) {
//        System.out.println("INSIDE INITIALIZE");
        sendButton = (Button) view.findViewById(R.id.button_gchat_send);
        messageToBeSent = (EditText) view.findViewById(R.id.edit_gchat_message);
//        System.out.println("Before Recylcer");
        mMessageRecycler = (RecyclerView) view.findViewById(R.id.recycler_gchat);
//        System.out.println("After Recycler");
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendMessage() {
        ServerConnector.sendMessage(chatID, messageToBeSent.getText().toString(), result -> {
            if (result.isFailure()) {
//                System.out.println("FAILURE OF THE MESSAGE BEING SENT");
                new StringErrorDialog(result.errorMessage)
                        .show(getParentFragmentManager(), null);
            }

//            System.out.println("SENDING MESSAGES DIDN'T FAIL");

            MessageID messageID= new MessageID(chatID, chatPosition + 1);

            UserMessage userMessage = new UserMessage(messageID, ServerConnector.getUser(), Instant.now().getEpochSecond(), messageToBeSent.getText().toString());
            MessageEntry messageEntry = new MessageEntry(userMessage.getMessageID(), userMessage.getUserInfo().id, userMessage.getTime2(), userMessage.getMessage());

            ServerConnector.getMessages(chatID, null, null, result1 -> {
                if (result1.isFailure()) {
 //                   System.out.println("RESULT1 IS FAILING NOW!");
                    return;
                }

                messageEntryArrayList.clear();
                messageEntryArrayList.addAll(Arrays.asList(result1.data));
 //               System.out.println("REMAKING THE ADAPTER");
                mMessageAdapter = new MessageListAdapter(getActivity(), messageEntryArrayList);
 //               System.out.println("ADAPTER REMADE");
            });
        });
    }
}