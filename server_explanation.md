
# ServerConnector Explanation

The main way to interact with the server is through the `ServerConnector` class's static methods.
Because requests to the server take a long time, these methods don't directly return their results.
Instead, they take a callback (`ResultHandler<T>`) which will be called whenever the server replies
to the request. You can directly create classes which implement this interface, but it is easier to
use *lambda expressions*. This special syntax:

```java
result -> {
    // ...
}
```

is the same as the much longer anonymous class:

```java
new ResultHandler<T>() {
    @Override
    public void handleResult(ServerResult<T> result) {
        // ...
    }
}
```

All of the `ServerConnector` functions that send requests take a callback like this, and the lambda
expression syntax will be used in all of these examples. The callback is allowed to be `null`, in
which case the response of the server is ignored completely.

The argument passed to the callback is a `ServerResult<T>`. This type represents either a successful
request with data of type `T`, or a failed request with an error message. The returned data is
available in `result.data` and the error message is available in `result.errorMessage`. To check
whether the request was successful or failed, use `result.isSuccess()` or `result.isFailure()`.

## Authentication

#### Logging in

```java
ServerConnector.login(email, password, result -> {
    if (result.isFailure()) {
        // Handle connection failure
        return;
    }

    LoginStatus status = result.data;
    switch (status) {
        case LoginStatus.SUCCESS:
            // Handle successful login
            break;
        case LoginStatus.ENTER_CODE:
            // Confirm email address
            break;
        case LoginStatus.DOES_NOT_EXIST:
            // Handle account doesn't exist
            break;
        case LoginStatus.INVALID_PASSWORD:
            // Handle invalid password
            break;
    }
});
```

#### Logging out

```java
ServerConnector.logout();
```

#### Creating an account

```java
ServerConnector.createAccount(name, email, password, result -> {
    if (result.isFailure()) {
        // Handle connection failure
        return;
    }

    boolean created = result.data;
    if (created) {
        // Confirm email address
    } else {
        // Handle account already exists
    }
});
```

#### Confirming an email address

When a user creates an account or resets their password, they will have to enter a code that was
sent to their email address before they can log in.

```java
ServerConnector.enterCode(code, result -> {
    if (result.isFailure()) {
        // Handle connection failure
        return;
    }

    boolean correct = result.data;
    if (correct) {
        // Open next menu
    } else {
        // Handle incorrect code
    }
});
```

#### Resetting a password

Resetting a password involves 3 steps. First, you must request a password reset:

```java
ServerConnector.requestReset(email, result -> {
    if (result.isFailure()) {
        // Handle connection failure
        return;
    }

    boolean sent = result.data;
    if (sent) {
        // Confirm email address
    } else {
        // Handle incorrect email address
    }
});
```

Next, you have to confirm the code sent to the email address with `ServerConnector.enterCode()`
just like after creating a new account.

Finally, you need to send the server the new password:

```java
ServerConnector.requestReset(password, result -> {
    if (result.isFailure()) {
        // Handle connection failure
        return;
    }

    // Handle successful login
});
```

## Creating groups and chats

#### Creating a group

```java
ServerConnector.createGroup(groupName, result -> {
    if (result.isFailure()) {
        // Handle connection failure
        return;
    }

    GroupID groupId = result.data;

    // Store the group data and open next menu
});
```

#### Creating a chat in a group

```java
ServerConnector.createChat(groupId, chatName, result -> {
    if (result.isFailure()) {
        // Handle connection failure
        return;
    }

    ChatID chatId = result.data;

    // Store the chat data and open next menu
});
```

## Listing groups, chats, and users

#### Listing all groups

```java
ServerConnector.getGroups(result -> {
    if (result.isFailure()) {
        // Handle connection failure
        return;
    }

    for (GroupEntry group : result.data) {
        // Do something with the group
    }
});
```

#### Listing all users in a group

```java
ServerConnector.getUsers(groupId, result -> {
    if (result.isFailure()) {
        // Handle connection failure
        return;
    }

    for (UserEntry user : result.data) {
        // Do something with the user
    }
});
```

#### Listing all chats in a group

```java
ServerConnector.getChats(groupId, result -> {
    if (result.isFailure()) {
        // Handle connection failure
        return;
    }

    for (ChatEntry chat : result.data) {
        // Do something with the chat
    }
});
```

## Sending and receiving messages

#### Sending messages in a chat

```java
ServerConnector.sendMessage(chatId, messageContents, result -> {
    if (result.isFailure()) {
        // Handle connection failure
        return;
    }

    // Display the message as being sent
});
```

#### Listing new messages in a chat on refresh

This code will only get messages that arrived after `lastReceivedMessageId`, but it may not return
an exhaustive list if a large number of messages were sent since the chat was last refreshed. If
this is the case, it will only return the most recently sent messages (older messages will be
missing). Therefore, it is important to discard the cached data if the messages being added aren't
immediately after the cached data. This can be checked with `MessageID.immediatelyAfter()`. To
handle new messages received without refreshing the chat window, see the section below about
notification listeners. See the documentation on `ServerConnector.getMessages()` for a description
of the arguments it accepts.

```java
ServerConnector.getMessages(chatId, lastReceivedMessageId, null, result -> {
    if (result.isFailure()) {
        // Handle connection failure
        return;
    }

    for (MessageEntry message : result.data) {
        // Add the message to the chat window (from oldest to newest)
    }
});
```

#### Listing older messages in a chat when scrolling backwards

```java
ServerConnector.getMessages(chatId, null, firstReceivedMessageId, result -> {
    if (result.isFailure()) {
        // Handle connection failure
        return;
    }

    MessageEntry[] messages = result.data;
    for (int i = messages.length - 1; i >= 0; i--) {
        MessageEntry message = messages[i];

        // Add the message to the chat window (from newest to oldest)
    }
});
```

## Notification listeners

A notification listener is a class which implements the `NotificationListener` interface. Whenever
the server sends the client a notification, the `ServerConnector` will forward the message to all
of the notification listeners.

#### Adding a notification listener

```java
ServerConnector.addNotificationListener(notificationListener);
```

#### Example notification listener

```java
class ExampleListener implements NotificationListener {
    @Override
    public void onReceiveMessage(MessageEntry message) {
        System.out.println(message.sender.name + " says: " + message.contents);
    }

    @Override
    public void onLoggedOut() {
        System.out.println("Logged out by server!");
    }
}
```

## Other requests

See the documentation on the static methods of `ServerConnector` for all supported request types.