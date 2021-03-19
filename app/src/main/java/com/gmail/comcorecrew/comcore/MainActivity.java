package com.gmail.comcorecrew.comcore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Bundle;
import android.view.Menu;

import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.fragments.MainFragment;
import com.gmail.comcorecrew.comcore.server.NotificationListener;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.connection.ServerConnection;
import com.gmail.comcorecrew.comcore.server.entry.GroupInviteEntry;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;
import com.gmail.comcorecrew.comcore.server.id.GroupID;

import java.util.Collection;

public class MainActivity extends AppCompatActivity implements NotificationListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppData.init(getApplicationContext());
        super.onCreate(savedInstanceState);

        // Initialize the connection to the server using the application context
        ServerConnector.setConnection(new ServerConnection(this.getBaseContext()));
        ServerConnector.addNotificationListener(this);

        setContentView(R.layout.activity_main);
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activitymenu, menu);
        return true;
    }

    @Override
    public void onReceiveMessage(MessageEntry message) {}

    @Override
    public void onInvitedToGroup(GroupInviteEntry invite) {}

    @Override
    public void onRoleChanged(GroupID group, GroupRole role) {}

    @Override
    public void onMuteChanged(GroupID group, boolean muted) {}

    @Override
    public void onKicked(GroupID group) {}

    @Override
    public void onLoggedOut() {}

    @Override
    public Collection<? extends NotificationListener> getChildren() {
        return MainFragment.groups;
    }
}
