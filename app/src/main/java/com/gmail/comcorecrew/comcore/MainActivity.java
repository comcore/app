package com.gmail.comcorecrew.comcore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.connection.ServerConnection;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the connection to the server using the application context
        ServerConnector.setConnection(new ServerConnection(this.getBaseContext()));

        setContentView(R.layout.activity_main);
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }


}
