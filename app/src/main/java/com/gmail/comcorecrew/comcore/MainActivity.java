package com.gmail.comcorecrew.comcore;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.connection.ServerConnection;
import com.gmail.comcorecrew.comcore.server.id.GroupID;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the connection to the server using the application context
        ServerConnector.setConnection(new ServerConnection(this.getBaseContext()));

        setContentView(R.layout.activity_main);
    }
}
