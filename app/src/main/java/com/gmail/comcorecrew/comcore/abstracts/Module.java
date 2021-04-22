package com.gmail.comcorecrew.comcore.abstracts;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.caching.GroupStorage;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.classes.LockObject;
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.enums.Mdid;
import com.gmail.comcorecrew.comcore.notifications.NotificationListener;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.ModuleID;

import java.io.IOException;
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
    private long lastUpdated; //Last time contents were updated
    private final LockObject cacheLock = new LockObject(); //Allows caching synchronization
    private transient int index; //index of the module
    private transient Runnable onUpdate;

    public Module(String name, ModuleID id, Group group, Mdid mdid) {
        if (id == null) {
            throw new IllegalArgumentException("ModuleID must not be null!");
        }
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
        this.name = name;
        id = null;
        this.group = group;
        this.mdid = mdid;
        muted = false;
        mentionMuted = false;
        mnum = -1;
    }

    /**
     * Initializes the module to the server.
     */
    public void init() {
        if (id != null) {
            return;
        }
        switch (mdid) {
            case CMSG: {
                ServerConnector.createChat(group.getGroupId(), name, result -> {
                    if (result.isFailure()) {
                        ErrorDialog.show(R.string.error_cannot_connect);
                        return;
                    }
                    id = result.data;
                    mnum = group.addModule(this);
                    afterCreate();
                });
                break;
            }
            case CTSK: {
                ServerConnector.createTaskList(group.getGroupId(), name, result -> {
                    if (result.isFailure()) {
                        throw new RuntimeException(result.errorMessage);
                    }
                    id = result.data;
                    mnum = group.addModule(this);
                    afterCreate();
                });
                break;
            }
            case CCLD: {
                ServerConnector.createCalendar(group.getGroupId(), name, result -> {
                    if (result.isFailure()) {
                        ErrorDialog.show(R.string.error_cannot_connect);
                        return;
                    }
                    id = result.data;
                    mnum = group.addModule(this);
                    afterCreate();
                });
                break;
            }
            case CPLS: {
                ServerConnector.createPollList(group.getGroupId(), name, result -> {
                    if (result.isFailure()) {
                        ErrorDialog.show(R.string.error_cannot_connect);
                        return;
                    }
                    id = result.data;
                    mnum = group.addModule(this);
                    afterCreate();
                });
                break;
            }
            default: {
                throw new RuntimeException("Invalid MDID");
            }
        }
    }

    /**
     * Constructor for initializing a custom module
     *
     * @param name name of the module
     * @param group group that contains the module
     */
    public Module(String name, Group group) {
        mdid = Mdid.CSTM;
        id = null;
        this.name = name;
        this.group = group;
        muted = false;
        mentionMuted = false;
        mnum = -1;
    }

    public void init(String type) {
        if (id != null || mdid != Mdid.CSTM) {
            throw new IllegalArgumentException("Invalid module.init() call!");
        }
        ServerConnector.createCustomModule(group.getGroupId(), name, type, result -> {
            if (result.isFailure()) {
                ErrorDialog.show(R.string.error_cannot_connect);
                return;
            }
            this.id = result.data;
            mnum = group.addModule(this);
            afterCreate();
            try {
                GroupStorage.storeModule(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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

    /**
     * Set the last updated timestamp, clearing the cache if it is newer than the stored timestamp
     *
     * @param cacheClearTimestamp the timestamp that the cache was cleared at
     */
    public void setLastUpdated(long cacheClearTimestamp) {
        if (cacheClearTimestamp > lastUpdated) {
            lastUpdated = cacheClearTimestamp;
            clearCache();
        }
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setCallback(Runnable onUpdate) {
        this.onUpdate = onUpdate;
    }

    public void didUpdate() {
        if (onUpdate != null) {
            onUpdate.run();
        }
    }

    /**
     * Gets an integer array containing the address of the module
     *
     * @return the address of the module
     */
    public int[] getAddress() {
        return new int[] {group.getIndex(), index};
    }

    public String getGroupIdString() {
        return group.getGroupId().id;
    }

    public String getLocatorString() {
        return getGroupIdString() + "/" + mdid + mnum;
    }

    /**
     * Send the data of the module to the cache
     */
    public void toCache() {
        didUpdate();
        synchronized (cacheLock) {
            readToCache();
        }
    }

    /**
     * Reads the data of the module from the cache
     */
    public void fromCache() {
        synchronized (cacheLock) {
            readFromCache();
        }
    }

    /**
     * DO NOT USE, ONLY IMPLEMENT
     * Abstract method for module to implement. Separate from toCache for synchronization.
     */
    protected abstract void readToCache();

    /**
     * DO NOT USE, ONLY IMPLEMENT
     * Abstract method for module to implement. Separate from fromCache for synchronization.
     */
    protected abstract void readFromCache();

    public abstract void refresh();

    public void clearCache() {}

    /**
     * Run when a group is deleted to cancel any scheduled reminders.
     */
    public void onDeleted() {}

    /**
     * Run after the module is initialized to the server to avoid
     * race conditions
     */
    public void afterCreate() {}
}
