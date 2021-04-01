package com.gmail.comcorecrew.comcore.classes;

import android.os.Parcel;
import android.os.Parcelable;

import com.gmail.comcorecrew.comcore.abstracts.Module;
import com.gmail.comcorecrew.comcore.caching.UserStorage;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.enums.Mdid;
import com.gmail.comcorecrew.comcore.notifications.NotificationListener;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.gmail.comcorecrew.comcore.server.id.ModuleID;
import com.gmail.comcorecrew.comcore.server.id.UserID;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class Group implements NotificationListener {

    private GroupID groupID;
    private String groupName;
    private GroupRole groupRole;
    private boolean isMuted;
    private boolean isPinned;
    private ArrayList<Module> modules;
    private ArrayList<User> users;
    private ArrayList<UserID> moderators;
    private UserID owner;
    private transient int index;

    public Group(GroupID groupID) {
        this.groupID = groupID;
        modules = new ArrayList<>();
        index = -1;
    }

    public Group(String name, GroupID groupID, GroupRole groupRole, boolean isMuted) {
        this.groupID = groupID;
        this.groupName = name;
        this.groupRole = groupRole;
        users = new ArrayList<>();
        ServerConnector.getUsers(groupID, result -> {
            // TODO use cached user data with name
            for (int i = 0; i < result.data.length; i++) {
                User nextUser = new User(result.data[i].id, "<NAME>");
                users.add(nextUser);
            }
        });

        this.isMuted = isMuted;
        modules = new ArrayList<>();
        users = new ArrayList<>();
        moderators = new ArrayList<>();
        /** Commenting out this line so that the app will run **/
        //owner = UserStorage.getUser(0).getID();

        File cacheDir = new File(AppData.cacheDir, this.groupID.id);
        cacheDir.mkdir();
    }

    protected Group(Parcel in) {
        groupName = in.readString();
        byte tmpIsMuted = in.readByte();
        isMuted = tmpIsMuted == 0 ? null : tmpIsMuted == 1;
    }

    public String getName() {
        return groupName;
    }

    public void setName(String name) {
        this.groupName = name;
    }

    public GroupID getGroupId() {
        return groupID;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public ArrayList<UserID> getModerators() {
        return moderators;
    }

    public void setModerators(ArrayList<UserID> moderators) {
        this.moderators = moderators;
    }

    public GroupRole getGroupRole() {
        return groupRole;
    }

    public void setGroupRole(GroupRole groupRole) {
        this.groupRole = groupRole;
    }

    public boolean getIsMuted() {
        return isMuted;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public UserID getOwner() {
        return owner;
    }

    public void setOwner(UserID owner) {
        this.owner = owner;
    }

    public ArrayList<Module> getModules() {
        return modules;
    }

    public void setModules(ArrayList<Module> modules) {
        this.modules = modules;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int addModule(Module module) {
        int num = 0;
        Mdid mdid = module.getMdid();
        for(Module m : modules) {
            if ((m.getMdid() == mdid) && (m.getMnum() >= num)) {
                num = m.getMnum() + 1;
            }
        }
        modules.add(module);
        return num;
    }

    /**
     * Gets the requested module
     *
     * @param moduleID module id of the module to return
     * @return         the requested module; null if it does not exist
     */
    public Module getModule(ModuleID moduleID) {
        for (Module module : modules) {
            if (moduleID.id.equals(module.getId().id)) {
                return module;
            }
        }
        return null;
    }

    /**
     * Gets the requested module
     *
     * @param index the index of the module
     * @return      the requested module; null if it does not exist
     */
    public Module getModule(int index) {
        if ((index < 0) || (index > modules.size())) {
            return null;
        }
        return modules.get(index);
    }

    /**
     * Removes the module from the module list
     *
     * @param module the module to remove
     */
    public void deleteModule(Module module) {
        deleteModule(module.getIndex());
    }

    /**
     * Removes the module from the module list
     *
     * @param moduleID the module to remove
     */
    public void deleteModule(ModuleID moduleID) {
        for (int i = 0; i < modules.size(); i++) {
            if (moduleID.equals(modules.get(i).getId())) {
                deleteModule(i);
                return;
            }
        }
    }

    /**
     * Removes the module from the module list
     *
     * @param index the index of the module to remove
     */
    public void deleteModule(int index) {
        for (int i = index; i < modules.size() - 1; i++) {
            modules.set(i, modules.get(i + 1));
            modules.get(i).setIndex(i);
        }
        modules.remove(modules.size());
    }

    @Override
    public void onRoleChanged(GroupID group, GroupRole role) {
        groupRole = role;
    }

    @Override
    public void onMuteChanged(GroupID group, boolean muted) {
        isMuted = muted;
    }

    @Override
    public Collection<? extends NotificationListener> getChildren() {
        return modules;
    }
}
