package com.gmail.comcorecrew.comcore.abstracts;

import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.enums.Mdid;
import com.gmail.comcorecrew.comcore.notifications.NotificationListener;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
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
    private boolean mentionMuted; //Contains mention muted status of module notifications

    public Module(String name, ModuleID id, Group group, Mdid mdid) {
        this.name = name;
        this.id = id;
        this.group = group;
        this.mdid = mdid;
        muted = false;
        mentionMuted = false;
        mnum = group.addModule(this);
    }

    /**
     * Constructor for initializing a module
     *
     * @param name name of the group
     * @param group group that the module is in
     * @param mdid mdid of the group to add
     */
    public Module(String name, Group group, Mdid mdid) {
        switch (mdid) {
            case CMSG: {
                ServerConnector.createChat(group.getGroupId(), name, result -> {
                    if (result.isFailure()) {
                        throw new RuntimeException(result.errorMessage);
                    }
                    id = result.data;
                });
                break;
            }
            case CTSK: {
                ServerConnector.createTaskList(group.getGroupId(), name, result -> {
                    if (result.isFailure()) {
                        throw new RuntimeException(result.errorMessage);
                    }
                    id = result.data;
                });
                break;
            }
            default: {
                throw new RuntimeException("Invalid MDID");
            }
        }
        this.name = name;
        this.group = group;
        this.mdid = mdid;
        muted = false;
        mentionMuted = false;
        mnum = group.addModule(this);
    }

    /**
     * Constructor for initializing a custom module
     *
     * @param name name of the module
     * @param group group that contains the module
     * @param type custom type of the module
     */
    public Module(String name, Group group, String type) {
        ServerConnector.createCustomModule(group.getGroupId(), name, type, result -> {
            if (result.isFailure()) {
                throw new RuntimeException(result.errorMessage);
            }
            this.id = result.data;
        });
        this.name = name;
        this.group = group;
        mdid = Mdid.CSTM;
        muted = false;
        mentionMuted = false;
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

    public void setMentionMuted(boolean mentionMuted) {
        this.mentionMuted = mentionMuted;
    }

    public boolean isMentionMuted() {
        return mentionMuted;
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
