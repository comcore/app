package com.gmail.comcorecrew.comcore;

import com.gmail.comcorecrew.comcore.classes.User;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.ServerResult;
import com.gmail.comcorecrew.comcore.server.connection.MockConnection;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;
import com.gmail.comcorecrew.comcore.server.entry.ReactionEntry;
import com.gmail.comcorecrew.comcore.server.id.ChatID;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.gmail.comcorecrew.comcore.server.id.MessageID;
import com.gmail.comcorecrew.comcore.server.id.UserID;
import com.gmail.comcorecrew.comcore.server.info.UserInfo;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Tests that encoding and decoding a message doesn't change the contents of messages, even if they
 * contain non-ASCII Unicode characters such as emojis or characters from foreign scripts.
 */
public class MessageEncodingUnitTest {
    @Test
    public void encodeMessage() {
        ChatID chatID = new ChatID(new GroupID("gid"), "cid");

        // Create a message with unicode contents
        ReactionEntry reaction = new ReactionEntry(new UserID("other"), "like");
        MessageEntry entry = new MessageEntry(
                new MessageID(chatID, 1),
                new UserID("uid"),
                System.currentTimeMillis(),
                "Test message வணக்கம் \uD83D\uDC4D \uD83C\uDDFA\uD83C\uDDF8",
                Collections.singletonList(reaction));

        // Encode it in JSON as if receiving from the server
        JsonObject reactionJson = new JsonObject();
        reactionJson.addProperty("user", reaction.user.id);
        reactionJson.addProperty("reaction", reaction.reaction);

        JsonArray reactions = new JsonArray();
        reactions.add(reactionJson);

        JsonObject message = new JsonObject();
        message.addProperty("id", entry.id.id);
        message.addProperty("sender", entry.sender.id);
        message.addProperty("timestamp", entry.timestamp);
        message.addProperty("contents", entry.contents);
        message.add("reactions", reactions);

        JsonArray messages = new JsonArray();
        messages.add(message);

        JsonObject response = new JsonObject();
        response.add("messages", messages);
        ServerConnector.setConnection(new MockConnection(ServerResult.success(response)));

        // Check that the decoding of the message produces the same message that was sent
        ServerConnector.getMessages(entry.id.module, null, null, result -> {
            assertTrue(result.isSuccess());
            assertEquals(result.data.length, 1);
            assertEquals(result.data[0], entry);
        });
    }
}