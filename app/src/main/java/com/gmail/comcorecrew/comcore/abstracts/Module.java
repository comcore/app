package com.gmail.comcorecrew.comcore.abstracts;

import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.enums.Mdid;
import com.gmail.comcorecrew.comcore.server.NotificationListener;
import com.gmail.comcorecrew.comcore.server.id.ModuleID;

import java.io.Serializable;

/**
 * Abstract module class that stores basic module info. Recommended for implementation of modules.
 */
public abstract class Module implements Serializable, NotificationListener {

    private String name; //Name of the module
    private ModuleID id; //ModuleID
    private transient Group group; //Group that contains the module
    private final Mdid mdid; //Specific module identifier
    private int mnum; //Distinguishes same type modules
    private boolean muted; //Contains muted status of module notifications

    public Module(String name, ModuleID id, Group group, Mdid mdid) {
        this.name = name;
        this.id = id;
        this.group = group;
        this.mdid = mdid;
        mnum = group.addModule(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ModuleID getId() {
        return id;
    }

    public void setId(ModuleID id) {
        this.id = id;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Mdid getMdid() {
        return mdid;
    }

    public int getMnum() {
        return mnum;
    }

    public void setMnum(int mnum) {
        this.mnum = mnum;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public boolean isMuted() {
        return muted;
    }

    public String getGroupIdString() {
        return group.getGroupId().id;
    }

    public String getLocatorString() {
        return getGroupIdString() + "/" + mdid + mnum;
    }

    public abstract void toCache();

    public abstract void fromCache();
}
