package com.gmail.comcorecrew.comcore.interfaces;

/*
 * Basic interface for the module class.
 */
public interface Module {

    //Returns the id unique to the module type.
    String getMdid();

    //Returns the module number, which identifies the module from others in the group.
    int getMnum();

    //Caches the data of the module and returns true if successful.
    boolean toCache();

    //Returns the id of the group that the module is in.
    int getGroupId();
}
