package com.gmail.comcorecrew.comcore.classes;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.interfaces.Module;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.gmail.comcorecrew.comcore.server.id.UserID;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class Group implements Parcelable {
    private static int numGroups = 0;

    private UUID externalId;

    private String groupID;
    private String groupName;
    private GroupRole groupRole;
    private Boolean isMuted;

    private ArrayList<Module> modules;
    private ArrayList<User> users;
    private ArrayList<UserID> moderators;
    private UserID owner;

    public Group(Context context, String name, GroupID groupID, GroupRole groupRole, Boolean isMuted) {
        this.groupID = groupID.id;
        this.groupName = name;
        this.groupRole = groupRole;
        users = new ArrayList<User>();
        ServerConnector.getUsers(groupID, result -> {
            for (int i = 0; i < result.data.length; i++) {
                User nextUser = new User(result.data[i].id, result.data[i].name);
                users.add(nextUser);
            }
        });
        this.isMuted = isMuted;

        File cacheDir = new File(context.getCacheDir(), "gr" + this.groupID);
        cacheDir.mkdir();
    }

    protected Group(Parcel in) {
        groupID = in.readString();
        groupName = in.readString();
        byte tmpIsMuted = in.readByte();
        isMuted = tmpIsMuted == 0 ? null : tmpIsMuted == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(groupID);
        dest.writeString(groupName);
        dest.writeByte((byte) (isMuted == null ? 0 : isMuted ? 1 : 2));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    public String getName() {
        return groupName;
    }

    public void setName(String name) {
        this.groupName = name;
    }

    public String getGroupId() {
        return groupID;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public ArrayList<UserID> getModerators() {
        return moderators;
    }

    public GroupRole getGroupRole() {
        return groupRole;
    }

    public Boolean getIsMuted() {
        return isMuted;
    }

    public UserID getOwner() {
        return owner;
    }

    public ArrayList<Module> getModules() {
        return modules;
    }

    public int addModule(Module module) {
        int num = 0;
        String mdid = module.getMdid();
        for(Module m : modules) {
            if (m.getMdid().equals(mdid)) {
                num++;
            }
        }
        modules.add(module);
        return num;
    }
}
