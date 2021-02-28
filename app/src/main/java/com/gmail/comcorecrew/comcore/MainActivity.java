package com.gmail.comcorecrew.comcore;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.connector.ServerConnectorImpl;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ServerConnector.setInstance(new ServerConnectorImpl(this.getBaseContext()));
        setContentView(R.layout.activity_main);
    }
}