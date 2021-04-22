package com.gmail.comcorecrew.comcore.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.OpenableColumns;
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
import com.gmail.comcorecrew.comcore.classes.modules.PinnedMessages;
import com.gmail.comcorecrew.comcore.dialogs.AddReactionDialog;
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
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


    private static final int MY_REQUEST_CODE_PERMISSION = 1000;
    private static final int MY_RESULT_CODE_FILECHOOSER = 2000;
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
            default:
                return true;
        }
    }

    // Initializes objects in GUI
    public void initialize(View view) {
        toolBar = (Toolbar) view.findViewById(R.id.toolbar_gchannel);
        toolBar.setTitle(((ChatID) messaging.getId()).id);
        getActivity().setTitle(messaging.getName());
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
                ErrorDialog.show(result.errorMessage);
                return;
            }
            messaging.onReceiveMessage(result.data);
            refresh();
            messageToBeSent.getText().clear();
        });
    }

    // Used for deleteMessage() and editMessage()
    public void sendMessage(MessageEntry messageEntry1) {
        // If you're editing a message
        if (isEditMode) {
            if (messageToBeSent.getText().toString() == null | messageToBeSent.getText().toString().trim().isEmpty()) {
                return;
            }
            ServerConnector.updateMessage(messageEntry1.id, messageToBeSent.getText().toString(), result -> {
                if (result.isFailure()) {
                    ErrorDialog.show(result.errorMessage);
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
                    ErrorDialog.show(result.errorMessage);
                } else {
                    messaging.onMessageUpdated(result.data);
                    refresh();
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
        System.out.println(x);
    }

    private void reactMessage(MenuItem item) {
        messageEntry = messaging.getEntry(item.getGroupId());
        new AddReactionDialog(messageEntry, messageEntry.id, 0).show(getParentFragmentManager(), null);
    }

    private void uploadFile() {
        askPermissionAndBrowseFile();
    }

    private void askPermissionAndBrowseFile()  {
        // With Android Level >= 23, you have to ask the user
        // for permission to access External Storage.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) { // Level 23

            // Check if we have Call permission
            int permisson = ActivityCompat.checkSelfPermission(this.getContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE);

            if (permisson != PackageManager.PERMISSION_GRANTED) {
                // If don't have permission so prompt the user.
                this.requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_REQUEST_CODE_PERMISSION
                );
                return;
            }
        }
        this.doBrowseFile();
    }

    private void doBrowseFile()  {
        Intent chooseFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFileIntent.setType("*/*");
        // Only return URIs that can be opened with ContentResolver
        chooseFileIntent.addCategory(Intent.CATEGORY_OPENABLE);

        chooseFileIntent = Intent.createChooser(chooseFileIntent, "Choose a file");
        startActivityForResult(chooseFileIntent, MY_RESULT_CODE_FILECHOOSER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MY_RESULT_CODE_FILECHOOSER && resultCode == RESULT_OK && data != null && data.getData() != null) {

            filePath = data.getData();

            InputStream iStream = null;
            try {
                iStream = getContext().getContentResolver().openInputStream(filePath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                byte[] inputData = getBytes(iStream);
                String x = getFileName(filePath);
                System.out.println(x);
                ServerConnector.uploadFile(x, inputData, result -> {
                    if (result.isFailure()) {
                        ErrorDialog.show(result.errorMessage);
                        return;
                    }
                    messageToBeSent.setText(result.data);
                    sendMessage();
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}