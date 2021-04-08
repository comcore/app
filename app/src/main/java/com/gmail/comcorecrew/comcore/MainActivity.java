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
import com.gmail.comcorecrew.comcore.notifications.NotificationHandler;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.connection.ServerConnection;
import com.gmail.comcorecrew.comcore.server.entry.InviteLinkEntry;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the application context
        Context context = getBaseContext();

        try {
            AppData.preInit(context);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Initialize the notification manager using the application context
        NotificationHandler notificationHandler = new NotificationHandler(context);

        // Initialize the connection to the server
        ServerConnector.setConnection(new ServerConnection());
        ServerConnector.addNotificationListener(notificationHandler);

        // Get the URL if the user clicked a link
        Intent appLinkIntent = getIntent();
        String appLinkData = appLinkIntent.getDataString();

        // Check if the URL is valid with the server
        if (appLinkData != null) {
            ServerConnector.checkInviteLink(appLinkData, result -> {
                if (result.isFailure()) {
                    System.out.println(result.errorMessage);
                    new ErrorDialog(R.string.error_cannot_connect)
                            .show(getSupportFragmentManager(), null);
                    return;
                }

                // Check if the link is invalid
                InviteLinkEntry inviteLink = result.data;
                if (inviteLink == null) {
                    new ErrorDialog(R.string.error_link_invalid)
                            .show(getSupportFragmentManager(), null);
                    return;
                }

                // The link is valid, so store it for after logging in
                InviteLinkDialog.setLink(inviteLink);
                InviteLinkDialog.checkExpired(getSupportFragmentManager());
            });
        }

        // Create the main view
        setContentView(R.layout.activity_main);
        Toolbar mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activitymenu, menu);
        return true;
    }
}
