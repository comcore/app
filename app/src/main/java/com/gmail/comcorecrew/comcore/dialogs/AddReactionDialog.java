package com.gmail.comcorecrew.comcore.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.enums.Reaction;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.EventEntry;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;
import com.gmail.comcorecrew.comcore.server.entry.ReactionEntry;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.gmail.comcorecrew.comcore.server.id.MessageID;

public class AddReactionDialog extends DialogFragment {

    private final int message;
    private MessageID messageID;
    private final MessageEntry messageEntry;

    public AddReactionDialog(MessageEntry messageEntry, MessageID messageID, int message) {
        this.message = message;
        this.messageID = messageID;
        this.messageEntry = messageEntry;
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

        ImageButton imageButton = (ImageButton) view.findViewById(R.id.choose_reaction_thumbs_up);
        ImageButton imageButton1 = (ImageButton) view.findViewById(R.id.choose_reaction_thumbs_down);


        view.findViewById(R.id.choose_reaction_thumbs_down).setOnClickListener(clickedView -> {
            ServerConnector.addReaction(messageID, Reaction.DISLIKE, result -> {
                if (result.isFailure()) {
                    ErrorDialog.show(result.errorMessage);
                } else {
                    System.out.println(result.data.toString());
                    this.dismiss();
                }
            });
        });
    }
}
