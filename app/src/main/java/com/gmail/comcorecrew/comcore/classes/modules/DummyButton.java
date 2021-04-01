package com.gmail.comcorecrew.comcore.classes.modules;

import android.view.View;

import androidx.annotation.NonNull;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.abstracts.CustomChat;
import com.gmail.comcorecrew.comcore.caching.CustomItem;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.CustomModuleID;


/**
 * Simple dummy class to show that custom modules work.
 *
 * If the state is 0, the color is blue.
 * If the state is 1, the color is red.
 */
public class DummyButton extends CustomChat {

    public DummyButton(String name, CustomModuleID id, Group group) {
        super(name, id, group);
    }

    public DummyButton(String name, Group group) {
        super(name, group, "dummy");
        sendMessage("0");
    }

    @Override
    public void viewInit(@NonNull View view) {
    }

    @Override
    public int getLayout() {
        return R.layout.fragment_custom_button;
    }

    @Override
    public void refreshView(View view) {

    }

    public int getState() {
        CustomItem lastItem = getLastItem();
        if ((lastItem == null) || (lastItem.getData().equals("1"))) {
            return 1;
        }
        return 0;
    }

    public int press() {
        if (getState() == 0) {
            sendMessage("1");
            return 1;
        }
        sendMessage("0");
        return 0;
    }
}
