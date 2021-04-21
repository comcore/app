package com.gmail.comcorecrew.comcore.helpers;

import android.content.Context;
import android.os.Build;
import android.text.method.LinkMovementMethod;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.caching.MessageItem;
import com.gmail.comcorecrew.comcore.caching.UserStorage;
import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.classes.modules.Messaging;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.enums.Reaction;
import com.gmail.comcorecrew.comcore.fragments.ChatFragment5;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class MessageListAdapter extends RecyclerView.Adapter {
    private Context context;
    public Messaging messaging;

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    public String[] numericMonths = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
    public String[] semanticMonths = {"January", "February", "March", "April", "May", "June", "July",
                                        "August", "September", "October", "November", "December"};

    public MessageListAdapter(Context context, Messaging messaging) {
        this.context = context;
        this.messaging = messaging;
    }

    @Override
    public int getItemCount() {
        return messaging.numEntries();
    }

    public int getItemViewType(int position) {
        MessageItem message = messaging.get(position);

        if (message.getId() == AppData.self.getInternalId()) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.from_me, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.from_them, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageItem message = messaging.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView messageText, timeText, nameText, dateText;
        ImageView thumbs_up, thumbs_down;

        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_gchat_message_me);
            messageText.setMovementMethod(LinkMovementMethod.getInstance());
            timeText = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_me);
            nameText = (TextView) itemView.findViewById(R.id.text_gchat_user_me);
            dateText = (TextView) itemView.findViewById(R.id.text_gchat_date_me);
            thumbs_up = (ImageView) itemView.findViewById(R.id.message_reaction1);
            thumbs_down = (ImageView) itemView.findViewById(R.id.message_reaction2);
        }

        void bind(MessageItem message) {
            messageText.setText(ChatMention.formatMentions(message.getData(), messaging.getGroup(), null));
            timeText.setText(format(message.getTimestamp()));
            dateText.setText(format2(message.getTimestamp()));
            nameText.setText(UserStorage.getUser(message.getId()).getName());
            messageText.setOnCreateContextMenuListener(this);
            int[] reactions = message.getReactions().getReactions();
            for (int i = 0; i < message.getReactions().getReactions().length; i++) {
                System.out.println(reactions[i]);
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(this.getAdapterPosition(), ChatFragment5.ID_EDIT_BUTTON, 0, "Edit");
            menu.add(this.getAdapterPosition(), ChatFragment5.ID_DELETE_BUTTON, 1, "Delete");
            menu.add(this.getAdapterPosition(), ChatFragment5.ID_REACT_BUTTON, 3, "React");
            if (messaging.getGroup().getGroupRole() != GroupRole.USER) {
                menu.add(this.getAdapterPosition(), ChatFragment5.ID_PIN_BUTTON, 2, "Pin");
            }
        }
    }

    public String format(long miliseconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(new Date(miliseconds));
    }

    public String format2(long miliseconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
        String x = sdf.format(new Date(miliseconds));
        for (int i = 0; i < numericMonths.length; i++) {
            if (x.substring(0, 2).equals(numericMonths[i])) {
                return semanticMonths[i] + " " + x.substring(3, 5);
            }
        }

        return sdf.format(new Date(miliseconds));
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView messageText, timeText, nameText, dateText;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_gchat_message_other);
            messageText.setMovementMethod(LinkMovementMethod.getInstance());
            timeText = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_other);
            nameText = (TextView) itemView.findViewById(R.id.text_gchat_user_other);
            dateText = (TextView) itemView.findViewById(R.id.text_gchat_date_other);
        }

        void bind(MessageItem message) {
            messageText.setText(ChatMention.formatMentions(message.getData(), messaging.getGroup(), null));
            timeText.setText(format(message.getTimestamp()));
            dateText.setText(format2(message.getTimestamp()));
            nameText.setText(UserStorage.getUser(message.getId()).getName());
            messageText.setOnCreateContextMenuListener(this);
            int[] reactions = message.getReactions().getReactions();
            for (int i = 0; i < message.getReactions().getReactions().length; i++) {
                System.out.println(reactions[i]);
            }
        }

        // Creates menu for each message with 3 options
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            if (messaging.getGroup().getGroupRole() != GroupRole.USER) {
                menu.add(this.getAdapterPosition(), ChatFragment5.ID_DELETE_BUTTON, 0, "Delete");
                menu.add(this.getAdapterPosition(), ChatFragment5.ID_PIN_BUTTON, 1, "Pin");
            }
            menu.add(this.getAdapterPosition(), ChatFragment5.ID_REACT_BUTTON, 3, "React");
        }
    }
}
