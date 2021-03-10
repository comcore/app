package com.gmail.comcorecrew.comcore.classes;

import android.content.Context;

import com.gmail.comcorecrew.comcore.interfaces.Module;
import com.gmail.comcorecrew.comcore.server.id.UserID;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class Group {
    private static int numGroups = 0;

    private UUID externalId;
    private String name;
    private int groupId;
    private ArrayList<Module> modules;
    private ArrayList<UserID> users;
    private ArrayList<UserID> moderators;
    private UserID owner;

    public Group(Context context, UUID externalId, String name, UserID owner) {
        numGroups++;
        groupId = numGroups;
        this.owner = owner;
        this.externalId = externalId;
        this.name = name;
        modules = new ArrayList<Module>();
        users = new ArrayList<UserID>();
        moderators = new ArrayList<UserID>();
        File cacheDir = new File(context.getCacheDir(), "gr" + groupId);
        cacheDir.mkdir();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGroupId() {
        return groupId;
    }

    public ArrayList<UserID> getUsers() {
        return users;
    }

    public ArrayList<UserID> getModerators() {
        return moderators;
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
