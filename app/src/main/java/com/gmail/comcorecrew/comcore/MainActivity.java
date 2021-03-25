package com.gmail.comcorecrew.comcore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;

import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.notifications.NotificationHandler;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.connection.ServerConnection;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the application context
        Context context = getBaseContext();

        // Initialize storage and caching
        try {
            AppData.init(context);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize the notification manager
        NotificationHandler notificationHandler = new NotificationHandler(context);

        // Initialize the connection to the server using the application context
        ServerConnector.setConnection(new ServerConnection(context));
        ServerConnector.addNotificationListener(notificationHandler);

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
