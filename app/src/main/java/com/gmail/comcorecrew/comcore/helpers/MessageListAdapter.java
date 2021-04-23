package com.gmail.comcorecrew.comcore.helpers;

import android.os.Build;
import android.text.method.LinkMovementMethod;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.caching.MessageItem;
import com.gmail.comcorecrew.comcore.caching.UserStorage;
import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.classes.modules.Messaging;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.enums.Reaction;
import com.gmail.comcorecrew.comcore.fragments.ChatFragment5;
import com.gmail.comcorecrew.comcore.server.entry.EventEntry;

import java.util.Date;

public class MessageListAdapter extends RecyclerView.Adapter {
    public Messaging messaging;

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    public MessageListAdapter(Messaging messaging) {
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
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.from_me, parent, false);
            return new MessageHolder(view, true);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.from_them, parent, false);
            return new MessageHolder(view, false);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageItem message = messaging.get(position);
        MessageItem previousMessage = position > 0 ? messaging.get(position - 1) : null;
        ((MessageHolder) holder).bind(message, previousMessage);
    }

    private class MessageHolder extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener {
        TextView messageText, timeText, nameText, dateText;
        ImageView thumbs_up, thumbs_down;

        MessageHolder(View itemView, boolean fromMe) {
            super(itemView);

            if (fromMe) {
                messageText = itemView.findViewById(R.id.me_message_text);
                timeText = itemView.findViewById(R.id.me_message_timestamp);
                nameText = itemView.findViewById(R.id.me_username);
                dateText = itemView.findViewById(R.id.me_message_date);
                thumbs_up = itemView.findViewById(R.id.me_reaction_like);
                thumbs_down = itemView.findViewById(R.id.me_reaction_dislike);
            } else {
                messageText = itemView.findViewById(R.id.other_message_text);
                timeText = itemView.findViewById(R.id.other_message_timestamp);
                nameText = itemView.findViewById(R.id.other_username);
                dateText = itemView.findViewById(R.id.other_message_date);
                thumbs_up = itemView.findViewById(R.id.other_reaction_like);
                thumbs_down = itemView.findViewById(R.id.other_reaction_dislike);
            }

            messageText.setMovementMethod(LinkMovementMethod.getInstance());
        }

        void bind(MessageItem message, MessageItem previousMessage) {
            String todayDate = EventEntry.dateFormat.format(new Date(message.getTimestamp()));
            if (previousMessage == null) {
                dateText.setVisibility(View.VISIBLE);
                dateText.setText(todayDate);
            } else {
                String prevDate = EventEntry.dateFormat.format(new Date(previousMessage.getTimestamp()));
                if (prevDate.equals(todayDate)) {
                    dateText.setVisibility(View.GONE);
                } else {
                    dateText.setVisibility(View.VISIBLE);
                    dateText.setText(todayDate);
                }
            }

            messageText.setText(ChatMention.formatMentions(message.getData(), messaging.getGroup(), null));
            timeText.setText(EventEntry.timeFormat.format(new Date(message.getTimestamp())));
            nameText.setText(UserStorage.getUser(message.getId()).getName());
            messageText.setOnCreateContextMenuListener(this);
            if (message.getReactions().getReactionCount(Reaction.DISLIKE) > 0) {
                thumbs_down.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    thumbs_down.onVisibilityAggregated(true);
                }
                thumbs_down.setOnCreateContextMenuListener(this);

            } else {
                thumbs_down.setVisibility(View.GONE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    thumbs_down.onVisibilityAggregated(false);
                }
            }


            if (message.getReactions().getReactionCount(Reaction.LIKE) > 0) {
                thumbs_up.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    thumbs_up.onVisibilityAggregated(true);
                }
                thumbs_up.setOnCreateContextMenuListener(this);
            } else {
                thumbs_up.setVisibility(View.GONE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    thumbs_up.onVisibilityAggregated(false);
                }
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            if (v.getId() == R.id.me_message_text || v.getId() == R.id.other_message_text) {
                boolean mine = v.getId() == R.id.me_message_text;
                GroupRole role = messaging.getGroup().getGroupRole();

                // Add the copy button for all users
                menu.add(getAdapterPosition(), ChatFragment5.ID_COPY_BUTTON, 0, "Copy Text");

                // Add the react button for all users
                menu.add(getAdapterPosition(), ChatFragment5.ID_REACT_BUTTON, 1, "React to Message");

                // Only add the edit button if the message was sent by the user
                if (mine) {
                    menu.add(getAdapterPosition(), ChatFragment5.ID_EDIT_BUTTON, 2, "Edit Message");
                }

                // Add the delete button if the message was sent by the user or they are a moderator
                if (mine || role != GroupRole.USER) {
                    menu.add(getAdapterPosition(), ChatFragment5.ID_DELETE_BUTTON, 3, "Delete Message");
                }

                // Only add the pin button for moderators
                if (role != GroupRole.USER) {
                    menu.add(getAdapterPosition(), ChatFragment5.ID_PIN_BUTTON, 4, "Pin Message");
                }
            }

            int x = 0;
            if (v.getId() == R.id.other_reaction_like || v.getId() == R.id.me_reaction_like) {
                menu.add(this.getAdapterPosition(), 225, 0, "Username");
                x = menu.getItem(0).getGroupId();
                menu.removeItem(225);
                menu.add(this.getAdapterPosition(), 200, 0, "Number of users who liked: " + messaging.get(x).getReactions().getReactionCount(Reaction.LIKE));
            }

            if (v.getId() == R.id.other_reaction_dislike || v.getId() == R.id.me_reaction_dislike) {
                menu.add(this.getAdapterPosition(), 325, 0, "Username");
                x = menu.getItem(0).getGroupId();
                menu.removeItem(325);
                menu.add(this.getAdapterPosition(), 201, 0, "Number of users who dislike: " + messaging.get(x).getReactions().getReactionCount(Reaction.DISLIKE));
            }
        }
    }
}
