package com.gmail.comcorecrew.comcore.abstracts;

import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.enums.Mdid;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.CustomModuleID;

import android.view.View;

import androidx.annotation.NonNull;

public abstract class CustomModule extends Module {

    String type;

    public CustomModule(String name, CustomModuleID id, Group group) {
        super(name, id, group, Mdid.CSTM);
    }

    public CustomModule(String name, Group group, String type) {
        super(name, group, type);
        this.type = type;
    }

    abstract public void viewInit(@NonNull View view);

    abstract public int getLayout();

    abstract public String getType();

}
