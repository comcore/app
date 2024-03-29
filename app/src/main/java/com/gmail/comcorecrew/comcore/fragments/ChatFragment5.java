package com.gmail.comcorecrew.comcore.fragments;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.classes.modules.PinnedMessages;
import com.gmail.comcorecrew.comcore.dialogs.AddReactionDialog;
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.helpers.ChatMention;
import com.gmail.comcorecrew.comcore.helpers.MessageListAdapter;
import com.gmail.comcorecrew.comcore.classes.modules.Messaging;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;
import com.gmail.comcorecrew.comcore.server.id.ChatID;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment5#newInstance} factory method to
 * create an instance of this fragment.
 */

public class ChatFragment5 extends Fragment {
    public static final int ID_EDIT_BUTTON = 121;
    public static final int ID_DELETE_BUTTON = 122;
    public static final int ID_PIN_BUTTON = 123;
    public static final int ID_REACT_BUTTON = 124;
    public static final int ID_COPY_BUTTON = 125;

    private static final int PERMISSION_CODE = 1000;
    private static final int FILECHOOSER_CODE = 2000;
    private Uri filePath;

    public static Messaging messaging;
    public static String fileUpload = "";

    private MessageEntry messageEntry;

    private Button sendButton;
    private EditText messageToBeSent;
    private Toolbar toolBar;

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
    public void onDestroy() {
        getActivity().setTitle("Comcore");
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return inflater.inflate(R.layout.fragment_chat5, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initialize(view);

        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setStackFromEnd(true);
        messageRecycler.setLayoutManager(manager);
        messageAdapter = new MessageListAdapter(messaging);
        messageRecycler.setAdapter(messageAdapter);
        messageRecycler.scrollToPosition(messageAdapter.getItemCount() - 1);
        messaging.setCallback(this::refresh);
        messaging.refresh();

        sendButton.setOnClickListener(new View.OnClickListener() {
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

    public void reSet() {
        messageRecycler.setAdapter(messageAdapter);
        messaging.refresh();
        refresh();
    }

    public void refresh() {
        messageAdapter.notifyDataSetChanged();
        messageRecycler.scrollToPosition(messageAdapter.getItemCount() - 1);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.chatmenu, menu);
        menu.setGroupVisible(R.id.pin_group, true);
    }

    /**
     * Handles click events for the option menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.go_back:
                /* Handle back button */
                NavHostFragment.findNavController(this).popBackStack();
                return true;
            case R.id.upload_file:
                /**Handle Upload File button **/
                uploadFile();
                return true;
            case R.id.create_pinned:
                /** Handle creating pinned messages module **/
                String pinnedTitle = messaging.getName() + " Pinned Messages";
                new PinnedMessages(pinnedTitle, messaging.getGroup(), (ChatID) messaging.getId());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Listens for selection of an item in the ContextMenu in messageAdapter
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case ID_EDIT_BUTTON:
                editMessage(item);
                return true;
            case ID_DELETE_BUTTON:
                deleteMessage(item);
                return true;
            case ID_PIN_BUTTON:
                pinMessage(item);
                return true;
            case ID_REACT_BUTTON:
                reactMessage(item);
                return true;
            case ID_COPY_BUTTON:
                copyMessage(item);
                return true;
            default:
                return false;
        }
    }

    // Initializes objects in GUI
    public void initialize(View view) {
        toolBar = (Toolbar) view.findViewById(R.id.toolbar_gchannel);
        toolBar.setTitle(((ChatID) messaging.getId()).id);
        getActivity().setTitle(messaging.getName());
        sendButton = (Button) view.findViewById(R.id.button_chat_send);
        messageToBeSent = (EditText) view.findViewById(R.id.chat_sendmessage_text);
        messageRecycler = (RecyclerView) view.findViewById(R.id.chat_recycler);
    }

    // Sends message to the server and updates the view.
    public void sendMessage() {
        String message = messageToBeSent.getText().toString().trim();
        if (message.isEmpty()) {
            return;
        }

        ChatID chatID = (ChatID) messaging.getId();
        ServerConnector.sendMessage(chatID, message, result -> {
            if (result.isFailure()) {
                ErrorDialog.show(result.errorMessage);
                return;
            }
            messaging.onReceiveMessage(result.data);
            messageToBeSent.getText().clear();
        });
    }

    // Used for deleteMessage() and editMessage()
    public void sendMessage(MessageEntry messageEntry1) {
        // If you're editing a message
        if (isEditMode) {
            String message = messageToBeSent.getText().toString().trim();
            if (message.isEmpty()) {
                return;
            }

            ServerConnector.updateMessage(messageEntry1.id, message, result -> {
                if (result.isFailure()) {
                    ErrorDialog.show(result.errorMessage);
                } else {
                    messaging.onMessageUpdated(result.data);
                    messageToBeSent.getText().clear();
                }
            });
            isEditMode = false;
            // If you're deleting a message
        } else {
            ServerConnector.updateMessage(messageEntry1.id, null, result -> {
                if (result.isFailure()) {
                    ErrorDialog.show(result.errorMessage);
                } else {
                    messaging.onMessageUpdated(result.data);
                    messageToBeSent.getText().clear();
                }
                isDeleteMode = false;
            });
        }
    }

    private void deleteMessage (MenuItem item){
        messageEntry = messaging.getEntry(item.getGroupId());
        isDeleteMode = true;
        sendMessage(messageEntry);
    }

    private void editMessage(MenuItem item) {
        messageEntry = messaging.getEntry(item.getGroupId());
        messageToBeSent.setText(messaging.get(item.getGroupId()).getData());
        isEditMode = true;
    }

    private void pinMessage(MenuItem item) {
        messageEntry = messaging.getEntry(item.getGroupId());
        int x = PinnedMessages.pinUnpinMessage(messageEntry);
    }

    private void reactMessage(MenuItem item) {
        int x = 2;
        messageEntry = messaging.getEntry(item.getGroupId());
        AddReactionDialog addReactionDialog = new AddReactionDialog(messageEntry, messageEntry.id, item.getGroupId(), x, this);
        addReactionDialog.show(getParentFragmentManager(), null);
    }

    private void copyMessage(MenuItem item) {
        String message = messaging.get(item.getGroupId()).getData();
        Group group = messaging.getGroup();
        CharSequence formatted = ChatMention.formatMentions(message, group, null);

        Context context = getContext();
        ClipboardManager clipboard = (ClipboardManager)
                context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText(null, formatted.toString()));
    }

    private void uploadFile() {
        getPermission();
    }

    private void getPermission()  {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) { // Level 23
            int permission = ActivityCompat.checkSelfPermission(this.getContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_CODE
                );
                return;
            }
        }
        this.findFile();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Browse files automatically after requesting permission
        if (requestCode == PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            findFile();
        }
    }

    private void findFile()  {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        // Only return URIs that can be opened with ContentResolver
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent = Intent.createChooser(intent, "Choose a file");
        startActivityForResult(intent, FILECHOOSER_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILECHOOSER_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            filePath = data.getData();

            InputStream inputStream = null;
            try {
                inputStream = getContext().getContentResolver().openInputStream(filePath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            try {
                byte[] fileAsBytes = getBytes(inputStream);
                String x = getFileName(filePath);
                ServerConnector.uploadFile(x, fileAsBytes, result -> {
                    if (result.isFailure()) {
                        ErrorDialog.show(result.errorMessage);
                        return;
                    }
                    messageToBeSent.setText(AppData.self.getName() + " shared: " + result.data);
                    sendMessage();
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public byte[] getBytes(InputStream iStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int buffer = 1024;
        byte[] bufferArray = new byte[buffer];

        int len = 0;
        while ((len = iStream.read(bufferArray)) != -1) {
            byteArrayOutputStream.write(bufferArray, 0, len);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public String getFileName(Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            Cursor mousePointer = getContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (mousePointer != null && mousePointer.moveToFirst()) {
                    fileName = mousePointer.getString(mousePointer.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                mousePointer.close();
            }
        }
        if (fileName == null) {
            fileName = uri.getPath();
            int cut = fileName.lastIndexOf('/');
            if (cut != -1) {
                fileName = fileName.substring(cut + 1);
            }
        }
        return fileName;
    }
}