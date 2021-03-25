package com.gmail.comcorecrew.comcore.classes.modules;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.fragments.ChatFragment;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;
import com.gmail.comcorecrew.comcore.server.id.ChatID;

import java.util.ArrayList;
import java.util.List;

public class ChatArrayAdapter extends ArrayAdapter<ChatMessage> {

        private TextView chatText;
        private Context context;
        private Messaging messaging;

//        @Override
//        public void add(ChatMessage object) {
//                chatMessageList.add(object);
//                super.add(object);
//        }

        public ChatArrayAdapter(Context context, int textViewResourceId) {
                super(context, textViewResourceId);
                messaging = new Messaging( "Chat", ChatFragment.chatID, AppData.groups.get(0));
                this.context = context;
        }

        public int getCount() {
                return this.messaging.messages.size();
        }

        public ChatMessage getItem(int index) {

                MessageEntry entry = this.messaging.getEntries().get(index);
                return new ChatMessage(true, entry.contents, entry.timestamp);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
                ChatMessage chatMessageObj = getItem(position);
                View row = convertView;
                LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if (chatMessageObj.left) {
                        row = inflater.inflate(R.layout.rightside, parent, false);
                }else{
                        row = inflater.inflate(R.layout.leftside, parent, false);
                }
                chatText = (TextView) row.findViewById(R.id.msgr);
                chatText.setText(chatMessageObj.message);
                return row;
        }
}