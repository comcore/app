package com.gmail.comcorecrew.comcore.classes.modules;

import android.view.View;

import androidx.annotation.NonNull;

import com.gmail.comcorecrew.comcore.abstracts.CustomChat;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.CustomModuleID;

public class DummyButton extends CustomChat {

    boolean pressed;

    public DummyButton(String name, CustomModuleID id, Group group) {
        super(name, id, group);
    }

    @Override
    public void viewInit(@NonNull View view) {

    }

    @Override
    public int getLayout() {
        return 0;
    }

    public DummyButton(String name, Group group) {
        super(name, group, "dummy");
        sendMessage("0");
        pressed = false;
    }

    public int press() {
        if (pressed) {
            pressed = false;
        }
        return 0;
    }
}
