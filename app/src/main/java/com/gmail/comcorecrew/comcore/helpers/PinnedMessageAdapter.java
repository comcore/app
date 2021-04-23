package com.gmail.comcorecrew.comcore.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.caching.UserStorage;
import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.server.entry.EventEntry;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PinnedMessageAdapter extends RecyclerView.Adapter {
    private Context context;
    private ArrayList<MessageEntry> messageEntry;

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    public PinnedMessageAdapter(Context context, ArrayList<MessageEntry> messageEntry) {
        this.context = context;
        this.messageEntry = messageEntry;
    }

    public void setMessageEntry(ArrayList<MessageEntry> messageEntry) {
        this.messageEntry = messageEntry;
    }

    @Override
    public int getItemCount() {
        return messageEntry.size();
    }

    public int getItemViewType(int position) {
        MessageEntry message = messageEntry.get(position);

        if (message.sender.equals(AppData.self.getID())) {
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
            return new PinnedMessageAdapter.SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.from_them, parent, false);
            return new PinnedMessageAdapter.ReceivedMessageHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageEntry message = messageEntry.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((PinnedMessageAdapter.SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((PinnedMessageAdapter.ReceivedMessageHolder) holder).bind(message);
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText, dateText;

        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.me_message_text);
            timeText = (TextView) itemView.findViewById(R.id.me_message_timestamp);
            nameText = (TextView) itemView.findViewById(R.id.me_username);
            dateText = (TextView) itemView.findViewById(R.id.me_message_date);
        }

        void bind(MessageEntry message) {
            messageText.setText(message.contents);
            timeText.setText(EventEntry.timeFormat.format(new Date(message.timestamp)));
            dateText.setText(EventEntry.dateFormat.format(new Date(message.timestamp)));
            UserStorage.lookup(message.sender, user -> {
                nameText.setText(user.getName());
            });
        }
    }

        private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
            TextView messageText, timeText, nameText, dateText;

            ReceivedMessageHolder(View itemView) {
                super(itemView);

                messageText = (TextView) itemView.findViewById(R.id.other_message_text);
                timeText = (TextView) itemView.findViewById(R.id.other_message_timestamp);
                nameText = (TextView) itemView.findViewById(R.id.other_username);
                dateText = (TextView) itemView.findViewById(R.id.other_message_date);
            }

            void bind(MessageEntry message) {
                messageText.setText(message.contents);
                timeText.setText(EventEntry.timeFormat.format(new Date(message.timestamp)));
                dateText.setText(EventEntry.dateFormat.format(new Date(message.timestamp)));
                UserStorage.lookup(message.sender, user -> {
                    nameText.setText(user.getName());
                });
            }
        }
}
