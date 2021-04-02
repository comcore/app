package com.gmail.comcorecrew.comcore.helpers;


import android.content.Context;
import android.os.Build;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.caching.UserStorage;
import com.gmail.comcorecrew.comcore.classes.modules.Messaging;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageListAdapter extends RecyclerView.Adapter {
    private Context context;
    public Messaging messaging;

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    public MessageListAdapter(Context context, Messaging messaging) {
        this.context = context;
        this.messaging = messaging;
    }

    @Override
    public int getItemCount() {
        return messaging.numEntries();
    }

    public int getItemViewType(int position) {
        MessageEntry message = messaging.getEntry(position);

        if (message.sender.equals(ServerConnector.getUser().id)) {
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageEntry message = messaging.getEntry(position);

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

        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_gchat_message_me);
            timeText = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_me);
            nameText = (TextView) itemView.findViewById(R.id.text_gchat_user_me);
            dateText = (TextView) itemView.findViewById(R.id.text_gchat_date_me);
        }

//    private class SentMessageHolder extends RecyclerView.ViewHolder {
//        TextView messageText, timeText, nameText;
//
//        SentMessageHolder(View itemView) {
//            super(itemView);
//
//            messageText = itemView.findViewById(R.id.text_gchat_message_me);
//            timeText = itemView.findViewById(R.id.text_gchat_timestamp_me);
//            nameText = itemView.findViewById(R.id.text_gchat_user_me);
//        }

        void bind(MessageEntry message) {
//            System.out.println("Inside BIND SENT MESSAGE");
//            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
//            LocalDateTime now = LocalDateTime.now();
            messageText.setText(message.contents.isEmpty() ? "[deleted]" : message.contents);
            //timeText.setText(dtf.format(now));
            timeText.setText(format(message.timestamp));
            dateText.setText(format2(message.timestamp));
            UserStorage.lookup(message.sender, user -> {
                nameText.setText(user.getName());
            });
            messageText.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(this.getAdapterPosition(), 121, 0, "Delete");
            menu.add(this.getAdapterPosition(), 122, 1, "Edit");
            menu.add(this.getAdapterPosition(), 123, 2, "Pin");
        }
    }

    public static String format(long miliseconds) {
        //  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(new Date(miliseconds));
    }

    public static String format2(long miliseconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
        return sdf.format(new Date(miliseconds));
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView messageText, timeText, nameText, dateText;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_gchat_message_other);
            timeText = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_other);
            nameText = (TextView) itemView.findViewById(R.id.text_gchat_user_other);
            dateText = (TextView) itemView.findViewById(R.id.text_gchat_date_other);
        }

//    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
//        TextView messageText, timeText, nameText;
//
//        ReceivedMessageHolder(View itemView) {
//            super(itemView);
//
//            messageText = itemView.findViewById(R.id.text_gchat_message_other);
//            timeText = itemView.findViewById(R.id.text_gchat_timestamp_other);
//            nameText = itemView.findViewById(R.id.text_gchat_user_other);
//        }

        void bind(MessageEntry message) {
 //           System.out.println("Inside BIND RECEIVED MESSAGE");
//            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
            messageText.setText(message.contents.isEmpty() ? "[deleted]" : message.contents);
//            timeText.setText(dtf.format(message.getTime()));
            timeText.setText(format(message.timestamp));
            dateText.setText(format2(message.timestamp));
            UserStorage.lookup(message.sender, user -> {
                nameText.setText(user.getName());
            });
            messageText.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(this.getAdapterPosition(), 121, 0, "Delete");
            menu.add(this.getAdapterPosition(), 122, 1, "Edit");
            menu.add(this.getAdapterPosition(), 123, 2, "Pin");
        }
    }
}
