package com.gmail.comcorecrew.comcore.Helpers;


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
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;

import com.gmail.comcorecrew.comcore.server.id.ChatID;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

public class MessageListAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private ArrayList<MessageEntry> mMessageList = new ArrayList<MessageEntry>(3);
    private ArrayList<UserMessage> userMessageArraylist = new ArrayList<UserMessage>(3);
    public ChatID chatID;
    public Group group;

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    public MessageListAdapter(Context context, ArrayList<MessageEntry> messageList) {
        mContext = context;
        mMessageList = messageList;
    }

//    public MessageListAdapter(Context context, ArrayList<UserMessage> messageList) {
//        mContext = context;
//        userMessageArraylist = messageList;
//    }
//
//    @Override
//    public int getItemCount() {
//        try {
//            return userMessageArraylist.size();
//        } catch (Exception e) {
//            System.out.println("FUCK YOU GUYS");
//        }
//        return 0;
//    }
//
//    public int getItemViewType(int position) {
//        System.out.println("Inside getItemViewType");
//        UserMessage message = userMessageArraylist.get(position);
//
//        if (message.senderName.equals(ServerConnector.getUser().name)) {
//            // If the current user is the sender of the message
//            return VIEW_TYPE_MESSAGE_SENT;
//        } else {
//            // If some other user sent the message
//            return VIEW_TYPE_MESSAGE_RECEIVED;
//        }
//    }
//
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        System.out.println("Inside onCreateViewHolder");
//        System.out.println(parent.getContext());
//        View view;
//
//        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
//            view = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.from_me, parent, false);
//            return new SentMessageHolder(view);
//        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
//            view = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.from_them, parent, false);
//            return new ReceivedMessageHolder(view);
//        }
//
//        return null;
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        UserMessage message = userMessageArraylist.get(position);
//
//        switch (holder.getItemViewType()) {
//            case VIEW_TYPE_MESSAGE_SENT:
//                ((SentMessageHolder) holder).bind(message);
//                break;
//            case VIEW_TYPE_MESSAGE_RECEIVED:
//                ((ReceivedMessageHolder) holder).bind(message);
//        }
//    }
//
//    private class SentMessageHolder extends RecyclerView.ViewHolder {
//        TextView messageText, timeText, nameText;
//
//        SentMessageHolder(View itemView) {
//            super(itemView);
//
//            messageText = (TextView) itemView.findViewById(R.id.text_gchat_message_me);
//            timeText = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_me);
//            nameText = (TextView) itemView.findViewById(R.id.text_gchat_user_me);
//        }
//
//        @RequiresApi(api = Build.VERSION_CODES.O)
//        void bind(UserMessage message) {
//            System.out.println("Inside BIND SENT MESSAGE");
//            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
//            LocalDateTime now = LocalDateTime.now();
//            messageText.setText(message.getMessage());
//            timeText.setText(dtf.format(now));
//            nameText.setText(ServerConnector.getUser().name);
//        }
//    }
//
//    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
//        TextView messageText, timeText, nameText;
//
//        ReceivedMessageHolder(View itemView) {
//            super(itemView);
//
//            messageText = (TextView) itemView.findViewById(R.id.text_gchat_message_other);
//            timeText = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_other);
//            nameText = (TextView) itemView.findViewById(R.id.text_gchat_user_other);
//        }
//
//        @RequiresApi(api = Build.VERSION_CODES.O)
//        void bind(UserMessage message) {
//            System.out.println("Inside BIND RECEIVED MESSAGE");
//            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
//            messageText.setText(message.getMessage());
//            timeText.setText(dtf.format(message.getTime()));
//            nameText.setText(message.getSenderName());
//        }
//    }
//}



    @Override
    public int getItemCount() {
        try {
//            System.out.println("SIZE WINS ALL BABY");
            return mMessageList.size();
        } catch (Exception e) {
 //           System.out.println("FUCK YOU GUYS");
        }
        return 0;
    }

//    public  convert(MessageEntry messageEntry) {
//        for (int i = 0; i < ; i++) {
//
//        }
//    }

    public int getItemViewType(int position) {
 //       System.out.println("Inside getItemViewType");
        UserMessage message = new UserMessage(mMessageList.get(position));

        if (message.getUserInfo().id.equals(ServerConnector.getUser().id)) {
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
//        System.out.println("Inside onCreateViewHolder");
//        System.out.println(parent.getContext());
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
        UserMessage message = new UserMessage(mMessageList.get(position));

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
        }
    }

//    private class SentMessageHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
//        TextView messageText, timeText, nameText;
//
//        SentMessageHolder(View itemView) {
//            super(itemView);
//
//            messageText = (TextView) itemView.findViewById(R.id.text_gchat_message_me);
//            timeText = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_me);
//            nameText = (TextView) itemView.findViewById(R.id.text_gchat_user_me);
//        }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;

        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_gchat_message_me);
            timeText = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_me);
            nameText = (TextView) itemView.findViewById(R.id.text_gchat_user_me);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        void bind(UserMessage message) {
//            System.out.println("Inside BIND SENT MESSAGE");
//            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
//            LocalDateTime now = LocalDateTime.now();
            messageText.setText(message.getMessage());
            //timeText.setText(dtf.format(now));
            timeText.setText(format(message.getTime2()));
            nameText.setText(ServerConnector.getUser().name);
//            messageText.setOnCreateContextMenuListener(this);
        }

//        @Override
//        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//            menu.add(this.getAdapterPosition(), 121, 0, "Delete");
//            menu.add(this.getAdapterPosition(), 122, 1, "Edit");
//            menu.add(this.getAdapterPosition(), 123, 2, "Pin");
//        }
    }

    public static String format(long mnSeconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(mnSeconds));
    }

//    private class ReceivedMessageHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
//        TextView messageText, timeText, nameText;
//
//        ReceivedMessageHolder(View itemView) {
//            super(itemView);
//
//            messageText = (TextView) itemView.findViewById(R.id.text_gchat_message_other);
//            timeText = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_other);
//            nameText = (TextView) itemView.findViewById(R.id.text_gchat_user_other);
//        }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_gchat_message_other);
            timeText = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_other);
            nameText = (TextView) itemView.findViewById(R.id.text_gchat_user_other);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        void bind(UserMessage message) {
 //           System.out.println("Inside BIND RECEIVED MESSAGE");
//            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
            messageText.setText(message.getMessage());
//            timeText.setText(dtf.format(message.getTime()));
            timeText.setText(format(message.getTime2()));
            nameText.setText(message.getSender().getName());
        }

//        @Override
//        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//            menu.add(this.getAdapterPosition(), 121, 0, "Delete");
//            menu.add(this.getAdapterPosition(), 122, 1, "Edit");
//            menu.add(this.getAdapterPosition(), 123, 2, "Pin");
//        }
    }
}
