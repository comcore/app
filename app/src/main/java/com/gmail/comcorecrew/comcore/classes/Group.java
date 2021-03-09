package com.gmail.comcorecrew.comcore.classes;

import android.content.Context;

import com.gmail.comcorecrew.comcore.interfaces.Module;

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
    private ArrayList<Integer> users;
    private ArrayList<Integer> moderators;
    private int owner;

    public Group(Context context, UUID externalId, String name, int owner) {
        numGroups++;
        groupId = numGroups;
        this.owner = owner;
        this.externalId = externalId;
        this.name = name;
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

    public ArrayList<Integer> getUsers() {
        return users;
    }

    public ArrayList<Integer> getModerators() {
        return moderators;
    }

    public int getOwner() {
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
