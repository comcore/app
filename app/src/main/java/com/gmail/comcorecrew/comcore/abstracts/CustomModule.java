package com.gmail.comcorecrew.comcore.abstracts;

import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.enums.Mdid;
import com.gmail.comcorecrew.comcore.server.id.CustomModuleID;

public abstract class CustomModule extends Module {

    public CustomModule(String name, CustomModuleID id, Group group) {
        super(name, id, group, Mdid.CSTM);
    }

}
