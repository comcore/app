package com.gmail.comcorecrew.comcore.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.caching.MessageItem;
import com.gmail.comcorecrew.comcore.enums.Reaction;
import com.gmail.comcorecrew.comcore.fragments.ChatFragment5;
import com.gmail.comcorecrew.comcore.helpers.MessageListAdapter;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;
import com.gmail.comcorecrew.comcore.server.id.MessageID;

import java.util.Arrays;

public class AddReactionDialog extends DialogFragment {

    private final int message;
    private MessageID messageID;
    private final MessageEntry messageEntry;
    public int x;
    private ChatFragment5 chatFragment5;

    public AddReactionDialog(MessageEntry messageEntry, MessageID messageID, int message, int x, ChatFragment5 chatFragment5) {
        this.message = message;
        this.messageID = messageID;
        this.messageEntry = messageEntry;
        this.x = x;
        this.chatFragment5 = chatFragment5;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.choose_reaction, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int myReaction = 0;

        ImageButton imageButton = (ImageButton) view.findViewById(R.id.choose_reaction_thumbs_up);
        ImageButton imageButton1 = (ImageButton) view.findViewById(R.id.choose_reaction_thumbs_down);
        MessageItem messageItem = new MessageItem(ChatFragment5.messaging.getEntry(message));

        for (int i = 0; i < ChatFragment5.messaging.messages.size(); i++) {
            if (ChatFragment5.messaging.messages.get(i).getData().equals(messageItem.getData())) {
                myReaction = ChatFragment5.messaging.messages.get(i).getMyReaction().toInt();
            }
        }


        int finalMyReaction = myReaction;
        view.findViewById(R.id.choose_reaction_thumbs_down).setOnClickListener(clickedView -> {
            if (finalMyReaction == Reaction.DISLIKE.toInt()) {
                ServerConnector.setReaction(messageID, Reaction.NONE, result -> {
                    if (result.isFailure()) {
                        ErrorDialog.show(result.errorMessage);
                        return;
                    } else {
                        ChatFragment5.messaging.onReactionUpdated(messageID, result.data);
                        this.dismiss();
                    }
                });
            } else {
                ServerConnector.setReaction(messageID, Reaction.DISLIKE, result -> {
                    if (result.isFailure()) {
                        ErrorDialog.show(result.errorMessage);
                        return;
                    } else {
                        ChatFragment5.messaging.onReactionUpdated(messageID, result.data);
                        this.dismiss();
                    }
                });
            }
        });

        view.findViewById(R.id.choose_reaction_thumbs_up).setOnClickListener(clickedView -> {
            if (finalMyReaction == Reaction.LIKE.toInt()) {
                System.out.println(messageItem.getMyReaction().toInt());
                ServerConnector.setReaction(messageID, Reaction.NONE, result -> {
                    if (result.isFailure()) {
                        ErrorDialog.show(result.errorMessage);
                        return;
                    } else {
                        ChatFragment5.messaging.onReactionUpdated(messageID, result.data);
                        this.dismiss();
                    }
                });
            } else {
                ServerConnector.setReaction(messageID, Reaction.LIKE, result -> {
                    if (result.isFailure()) {
                        ErrorDialog.show(result.errorMessage);
                        return;
                    } else {
                        ChatFragment5.messaging.onReactionUpdated(messageID, result.data);
                        this.dismiss();
                    }
                });
            }
        });
    }

}
