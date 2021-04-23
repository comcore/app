package com.gmail.comcorecrew.comcore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.dialogs.InviteLinkDialog;
import com.gmail.comcorecrew.comcore.fragments.LoginFragment;
import com.gmail.comcorecrew.comcore.notifications.NotificationHandler;
import com.gmail.comcorecrew.comcore.notifications.NotificationScheduler;
import com.gmail.comcorecrew.comcore.server.LoginToken;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.connection.ServerConnection;
import com.gmail.comcorecrew.comcore.server.entry.InviteLinkEntry;
import com.gmail.comcorecrew.comcore.server.info.UserInfo;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the application context
        Context context = getBaseContext();

        // Set the ErrorDialog's fragment manager
        ErrorDialog.fragmentManager = getSupportFragmentManager();

        // Initialize the notification manager using the application context
        NotificationHandler notificationHandler = new NotificationHandler(context);

        // Initialize the notification scheduler using the application context
        NotificationScheduler.init(context);

        // Initialize the connection to the server
        ServerConnector.setConnection(new ServerConnection());
        ServerConnector.setNotificationListener(notificationHandler);

        // Get the URL if the user clicked a link
        Intent appLinkIntent = getIntent();
        String appLinkData = appLinkIntent.getDataString();

        // Check if the URL is valid with the server
        if (appLinkData != null) {
            ServerConnector.checkInviteLink(appLinkData, result -> {
                if (result.isFailure()) {
                    ErrorDialog.show(R.string.error_cannot_connect);
                    return;
                }

                // Check if the link is invalid
                InviteLinkEntry inviteLink = result.data;
                if (inviteLink == null) {
                    ErrorDialog.show(R.string.error_link_invalid);
                    return;
                }

                // The link is valid, so store it for after logging in
                InviteLinkDialog.setLink(inviteLink);
                InviteLinkDialog.checkExpired();
            });
        }

        // Create the main view for the application
        setContentView(R.layout.activity_main);
        Toolbar mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        // Start initializing the cache using the application context
        try {
            AppData.preInit(context, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void whenExistingLogin(UserInfo info, LoginToken token) throws IOException {
        // Call AppData.init() first in case there is an error
        Context context = getBaseContext();
        AppData.init(info, token, context);

        // Connect to the server with the token
        ServerConnector.connect(token);

        // Set alreadyLoggedIn to true so that the LoginFragment knows to skip login
        LoginFragment.alreadyLoggedIn = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activitymenu, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        // Make sure the notification scheduler stores any changes
        NotificationScheduler.store();

        super.onDestroy();
    }
}
