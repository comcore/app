package com.gmail.comcorecrew.comcore;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.connection.ServerConnection;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the connection to the server using the application context
        ServerConnector.setConnection(new ServerConnection(this.getBaseContext()));
        ServerConnector.authenticate("a", "b", false, result -> {
            ServerConnector.setConnection(null);
        });

        setContentView(R.layout.activity_main);
    }
}
