package com.gmail.comcorecrew.comcore.fragments;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.caching.GroupStorage;
import com.gmail.comcorecrew.comcore.classes.modules.PinnedMessages;
import com.gmail.comcorecrew.comcore.dialogs.AddMemberDialog;
import com.gmail.comcorecrew.comcore.dialogs.CreateLinkDialog;
import com.gmail.comcorecrew.comcore.dialogs.CreateModuleDialog;
import com.gmail.comcorecrew.comcore.dialogs.ViewMembersDialog;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.helpers.MessageListAdapter;
import com.gmail.comcorecrew.comcore.classes.modules.Messaging;
import com.gmail.comcorecrew.comcore.dialogs.StringErrorDialog;
import com.gmail.comcorecrew.comcore.notifications.ChatMention;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;
import com.gmail.comcorecrew.comcore.server.id.ChatID;
import com.gmail.comcorecrew.comcore.server.id.MessageID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment5#newInstance} factory method to
 * create an instance of this fragment.
 */

public class ChatFragment5 extends Fragment {
    public static Messaging messaging;
    private MessageEntry messageEntry;

    private Button sendButton;
    private EditText messageToBeSent;

    private RecyclerView messageRecycler;
    private MessageListAdapter messageAdapter;

    boolean isEditMode = false;
    boolean isDeleteMode = false;

    public ChatFragment5() {
        // Required empty public constructor
    }

    public static ChatFragment5 newInstance() {
        ChatFragment5 fragment = new ChatFragment5();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        setHasOptionsMenu(true);

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

        initialize(view);

        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setStackFromEnd(true);
        messageRecycler.setLayoutManager(manager);
        messageAdapter = new MessageListAdapter(getContext(), messaging);
        messageRecycler.setAdapter(messageAdapter);
        messageRecycler.smoothScrollToPosition(messageAdapter.getItemCount());
        messaging.setCallback(this::refresh);
        messaging.refresh();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if (!isEditMode & !isDeleteMode) {
                    sendMessage();
                } else {
                    sendMessage(messageEntry);
                }
            }
        });
    }

    public void refresh() {
        messageAdapter.notifyDataSetChanged();
        messageRecycler.smoothScrollToPosition(messageAdapter.getItemCount());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (messaging.getGroup().getGroupRole() == GroupRole.OWNER || messaging.getGroup().getGroupRole() == GroupRole.MODERATOR) {
            menu.setGroupVisible(R.id.pin_group, true);
        }
        else {
            menu.setGroupVisible(R.id.pin_group, true);
        }

    }

    /**
     * Handles click events for the option menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.go_back:
                /** Handle back button **/
                NavHostFragment.findNavController(this).popBackStack();
                return true;

            case R.id.pinned_messages:
                /**Handle pinned messages button **/
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        messageAdapter.notifyDataSetChanged();
        messageRecycler.smoothScrollToPosition(messageAdapter.getItemCount());

    }

    // Listens for selection of an item in the ContextMenu in messageAdapter
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

    // Initializes objects in GUI
    public void initialize(View view) {
        sendButton = (Button) view.findViewById(R.id.button_gchat_send);
        messageToBeSent = (EditText) view.findViewById(R.id.edit_gchat_message);
        messageRecycler = (RecyclerView) view.findViewById(R.id.recycler_gchat);
    }

    // Sends message to the server and updates the view.
    public void sendMessage() {
        if (messageToBeSent.getText().toString() == null | messageToBeSent.getText().toString().trim().isEmpty()) {
            return;
        }

        ChatID chatID = (ChatID) messaging.getId();
        ServerConnector.sendMessage(chatID, messageToBeSent.getText().toString(), result -> {
            if (result.isFailure()) {
                new StringErrorDialog(result.errorMessage)
                        .show(getParentFragmentManager(), null);
            }
            messaging.onReceiveMessage(result.data);
            refresh();
            messageToBeSent.getText().clear();
        });
    }

    // Used for deleteMessage() and editMessage()
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendMessage(MessageEntry messageEntry1) {
        // If you're editing a message
        if (isEditMode) {
            if (messageToBeSent.getText().toString() == null | messageToBeSent.getText().toString().trim().isEmpty()) {
                return;
            }
            ServerConnector.updateMessage(messageEntry1.id, messageToBeSent.getText().toString(), result -> {
                if (result.isFailure()) {
                    new StringErrorDialog(result.errorMessage)
                            .show(getParentFragmentManager(), null);
                } else {
                    messaging.onMessageUpdated(result.data);
                    refresh();
                    messageToBeSent.getText().clear();
                }
            });
            isEditMode = false;
            // If you're deleting a message
        } else {
            ServerConnector.updateMessage(messageEntry1.id, null, result -> {
                if (result.isFailure()) {
                    new StringErrorDialog(result.errorMessage)
                            .show(getParentFragmentManager(), null);
                } else {
                    messaging.onMessageUpdated(result.data);
                    refresh();
                    messageToBeSent.getText().clear();
                    isDeleteMode = false;
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void deleteMessage (MenuItem item){
        messageEntry = messaging.getEntry(item.getGroupId());
        isDeleteMode = true;
        sendMessage(messageEntry);
    }

    private void editMessage(MenuItem item) {
        messageEntry = messaging.getEntry(item.getGroupId());
        messageToBeSent.setText(messaging.getEntries().get(item.getGroupId()).contents);
        isEditMode = true;
    }

    private void pinMessage(MenuItem item) {
        item.getGroupId();
        messageEntry = messaging.getEntry(item.getGroupId());
        boolean x = PinnedMessages.pinUnpinMessage(messageEntry);
        System.out.println(x);
    }
}