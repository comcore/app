package com.gmail.comcorecrew.comcore.classes;

import android.os.Parcel;

import androidx.annotation.NonNull;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.abstracts.Module;
import com.gmail.comcorecrew.comcore.caching.GroupStorage;
import com.gmail.comcorecrew.comcore.caching.UserStorage;
import com.gmail.comcorecrew.comcore.classes.modules.BulletinBoard;
import com.gmail.comcorecrew.comcore.classes.modules.Calendar;
import com.gmail.comcorecrew.comcore.classes.modules.DummyButton;
import com.gmail.comcorecrew.comcore.classes.modules.Messaging;
import com.gmail.comcorecrew.comcore.classes.modules.PinnedMessages;
import com.gmail.comcorecrew.comcore.classes.modules.Polling;
import com.gmail.comcorecrew.comcore.classes.modules.TaskList;
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.enums.Mdid;
import com.gmail.comcorecrew.comcore.notifications.NotificationListener;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.GroupUserEntry;
import com.gmail.comcorecrew.comcore.server.id.*;
import com.gmail.comcorecrew.comcore.server.info.ModuleInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Group implements NotificationListener, Comparable<Group> {

    private GroupID groupID;
    private String groupName;
    private GroupRole groupRole;
    private boolean isMuted;
    private boolean isPinned;
    private ArrayList<Module> modules;
    private transient ArrayList<ModuleInfo> disabledModules;
    private ArrayList<User> users;
    private ArrayList<UserID> moderators;
    private UserID owner;
    private transient int index;
    private boolean requireApproval;

    // This field is not stored and is only available when connected to the server
    private transient HashMap<UserID, Boolean> isUserMuted;

    public Group(GroupID groupID) {
        this.groupID = groupID;
        modules = new ArrayList<>();
        index = -1;
    }

    public Group(String name, GroupID groupID, GroupRole groupRole, boolean isMuted) {
        this.groupID = groupID;
        this.groupName = name;
        this.groupRole = groupRole;
        this.isMuted = isMuted;
        modules = new ArrayList<>();
        users = new ArrayList<>();
        moderators = new ArrayList<>();
        requireApproval = true;

        File cacheDir = new File(AppData.cacheDir, this.groupID.id);
        cacheDir.mkdir();

        refreshUsers(null, null);
    }

    public Group(String name, GroupID groupID, GroupRole groupRole, boolean isMuted, boolean isDirect) {
        this.groupID = groupID;
        this.groupName = name;
        this.groupRole = groupRole;
        this.isMuted = isMuted;
        modules = new ArrayList<>();
        users = new ArrayList<>();
        moderators = new ArrayList<>();
        requireApproval = true;

        File cacheDir = new File(AppData.cacheDir, this.groupID.id);
        cacheDir.mkdir();

        refreshUsers(null, null);
    }

    protected Group(Parcel in) {
        groupName = in.readString();
        byte tmpIsMuted = in.readByte();
        isMuted = tmpIsMuted == 0 ? null : tmpIsMuted == 1;
    }

    /**
     * Refresh all the users in the group. The already refreshed set is used to prevent fetching
     * user information that was just fetched, and it will be updated to contain any users that
     * were refreshed in addition to anything already in the set. The provided Runnable will be
     * called on completion. Stores the group to the cache after completion.
     *
     * @param alreadyRefreshed the already refreshed set (or null)
     * @param callback         the callback to run (or null)
     */
    public void refreshUsers(Set<UserID> alreadyRefreshed, Runnable callback) {
        // Iterate over every user in the group and update their information
        ServerConnector.getUsers(groupID, resultUsers -> {
            if (resultUsers.isFailure()) {
                if (callback != null) {
                    callback.run();
                }
                return;
            }

            // Find which users need to be refreshed still
            GroupUserEntry[] entries = resultUsers.data;
            ArrayList<UserID> toRefresh = new ArrayList<>();
            for (GroupUserEntry entry : entries) {
                if (alreadyRefreshed == null || alreadyRefreshed.add(entry.id)) {
                    toRefresh.add(entry.id);
                }
            }

            // Refresh all necessary user data
            UserStorage.refresh(toRefresh, () -> {
                // Find the users, moderators, and owner of the group
                ArrayList<User> groupUsers = new ArrayList<>(entries.length);
                ArrayList<UserID> groupModerators = new ArrayList<>();
                HashMap<UserID, Boolean> muted = new HashMap<>();
                UserID groupOwner = null;
                for (GroupUserEntry entry : entries) {
                    User user = UserStorage.getUser(entry.id);
                    if (user == null) {
                        System.err.println("no information for user: " + entry.id);
                        continue;
                    }

                    groupUsers.add(user);
                    muted.put(user.getID(), entry.muted);

                    if (entry.role == GroupRole.MODERATOR) {
                        groupModerators.add(entry.id);
                    } else if (entry.role == GroupRole.OWNER) {
                        if (groupOwner != null) {
                            throw new IllegalStateException("group has multiple owners");
                        }

                        groupOwner = entry.id;
                    }
                }

                // Update the user list of the group
                users = groupUsers;
                moderators = groupModerators;
                owner = groupOwner;
                isUserMuted = muted;

                // Store the updated group data
                try {
                    GroupStorage.storeGroup(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (callback != null) {
                    callback.run();
                }
            });
        });
    }

    /**
     * Refresh all the modules in the group, calling the provided Runnable on completion.
     *
     * @param callback the callback to run (or null)
     */
    public void refreshModules(Runnable callback) {
        HashMap<ModuleID, Module> ids = new HashMap<>(modules.size());
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            ModuleID id = module.getId();

            // Remove any duplicated modules
            if (ids.containsKey(id)) {
                deleteModule(i--);
                continue;
            }

            ids.put(id, module);
        }

        ServerConnector.getModules(groupID, result -> {
            if (result.isFailure()) {
                if (callback != null) {
                    callback.run();
                }
                return;
            }

            // Reset the disabled modules list
            disabledModules = new ArrayList<>();

            // Add any newly created modules to the group
            HashSet<ModuleID> keptIds = new HashSet<>();
            for (ModuleInfo info : result.data) {
                if (!info.enabled) {
                    disabledModules.add(info);
                    continue;
                }

                Module module = ids.get(info.id);
                if (module == null) {
                    module = createModule(info);
                    if (module == null) {
                        continue;
                    }
                } else {
                    keptIds.add(info.id);
                }

                // Update the module info
                module.setName(info.name);
                module.setLastUpdated(info.cacheClearTimestamp);

                // Tell the module to refresh its contents
                module.refresh();

                // Store the updated module data
                try {
                    GroupStorage.storeModule(module);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Remove any deleted modules from the group
            for (ModuleID id : ids.keySet()) {
                if (!keptIds.contains(id)) {
                    deleteModule(id);
                }
            }

            if (callback != null) {
                callback.run();
            }
        });
    }

    @NonNull
    public String getDisplayName() {
        // If the group is a direct message group, return the name of the other user instead
        if (isDirect() && users != null) {
            for (User user : users) {
                if (!user.getID().equals(AppData.self.getID())) {
                    return user.getName();
                }
            }
        }

        return groupName == null ? "" : groupName;
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
        if (moderators == null) {
            moderators = new ArrayList<>();
        }

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

    public boolean getMuted() {
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

    public ArrayList<ModuleInfo> getDisabledModules() {
        if (disabledModules == null) {
            disabledModules = new ArrayList<>(0);
        }
        return disabledModules;
    }

    public Boolean getUserMuted(UserID user) {
        return isUserMuted == null ? null : isUserMuted.get(user);
    }

    public void setUserMuted(UserID user, boolean muted) {
        if (isUserMuted == null) {
            isUserMuted = new HashMap<>();
        }

        isUserMuted.put(user, muted);
    }

    public GroupRole getRole(UserID user) {
        if (user.equals(AppData.self.getID())) {
            return groupRole;
        } else if (user.equals(owner)) {
            return GroupRole.OWNER;
        } else if (getModerators().contains(user)) {
            return GroupRole.MODERATOR;
        } else {
            return GroupRole.USER;
        }
    }

    public boolean isDirect() {
        return owner == null;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isRequireApproval() {
        return requireApproval;
    }

    public void setRequireApproval(boolean requireApproval) {
        this.requireApproval = requireApproval;
    }

    public void updateRequireApproval(boolean requireApproval) {
        ServerConnector.setRequireApproval(groupID, requireApproval, result -> {
            if (result.isFailure()) {
                ErrorDialog.show(R.string.error_cannot_connect);
                return;
            }

            this.requireApproval = requireApproval;
            if (!requireApproval) {
                ErrorDialog.show(R.string.event_approval_not_needed);
            }
            else {
                ErrorDialog.show(R.string.event_approval_needed);
            }

            try {
                GroupStorage.storeGroup(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Create a module from a ModuleInfo returned by the server.
     *
     * @param info the module info
     * @return the module if it is supported, otherwise null
     */
    public Module createModule(ModuleInfo info) {
        ModuleID id = info.id;
        if (id instanceof ChatID) {
            return new Messaging(info.name, (ChatID) id, this);
        } else if (id instanceof TaskListID) {
            return new TaskList(info.name, (TaskListID) id, this);
        } else if (id instanceof CalendarID) {
            return new Calendar(info.name, (CalendarID) id, this);
        } else if (id instanceof PollListID) {
            return new Polling(info.name, (PollListID) id, this);
        } else if (id instanceof CustomModuleID) {
            CustomModuleID customId = (CustomModuleID) id;
            switch (customId.type) {
                case "pinnedMessages":
                    return new PinnedMessages(info.name, customId, this, null);
                case "bulletin":
                    return new BulletinBoard(info.name, customId, this);
                case "dummy":
                    return new DummyButton(info.name, customId, this);
            }
        }

        return null;
    }

    public int addModule(Module module) {
        int num = 0;
        Mdid mdid = module.getMdid();
        Module m;
        for (int i = 0; i < modules.size(); i++) {
            m = modules.get(i);
            if (m.getMdid() == mdid) {
                if ((module.getId() != null) &&
                        module.getId().id.equals(m.getId().id)) {
                    //Duplicate found! Replace module and store module!
                    modules.set(i, module);
                    module.setIndex(m.getIndex());
                    module.setMnum(m.getMnum());
                    try {
                        GroupStorage.storeModule(module);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return module.getMnum();
                }
                else if (num <= m.getMnum()) {
                    num = m.getMnum() + 1;
                }
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
        modules.get(index).onDeleted();
        for (int i = index; i < modules.size() - 1; i++) {
            modules.set(i, modules.get(i + 1));
            modules.get(i).setIndex(i);
        }
        modules.remove(modules.size() - 1);
    }

    public void onDeleted() {
        for (Module module : modules) {
            module.onDeleted();
        }
    }

    @Override
    public void onRoleChanged(GroupID group, GroupRole role) {
        if (!group.equals(groupID)) {
            return;
        }

        groupRole = role;
        if (role == GroupRole.OWNER) {
            owner = AppData.self.getID();
        }
    }

    @Override
    public void onMuteChanged(GroupID group, boolean muted) {
        if (!group.equals(groupID)) {
            return;
        }

        isMuted = muted;
    }

    @Override
    public Collection<? extends NotificationListener> getChildren() {
        return modules;
    }

    @Override
    public int compareTo(Group o) {
        int compare;

        // Put pinned groups first
        if ((compare = Boolean.compare(o.isPinned, isPinned)) != 0) {
            return compare;
        }

        // Put direct messages first
        if ((compare = Boolean.compare(o.isDirect(), isDirect())) != 0) {
            return compare;
        }

        // Put moderated groups first
        GroupRole roleA = groupRole == null ? GroupRole.USER : groupRole;
        GroupRole roleB = o.groupRole == null ? GroupRole.USER : o.groupRole;
        if ((compare = roleB.compareTo(roleA)) != 0) {
            return compare;
        }

        // Sort by alphabetically based on group name next
        String a = getDisplayName();
        String b = o.getDisplayName();
        if ((compare = a.compareToIgnoreCase(b)) != 0) {
            return compare;
        }

        // Sort by group ID last
        a = groupID == null ? "" : groupID.id;
        b = o.groupID == null ? "" : o.groupID.id;
        return a.compareTo(b);
    }

    @Override
    @NonNull
    public String toString() {
        return getDisplayName();
    }
}
