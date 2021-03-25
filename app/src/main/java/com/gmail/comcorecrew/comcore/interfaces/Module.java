package com.gmail.comcorecrew.comcore.interfaces;

import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.enums.Mdid;
import com.gmail.comcorecrew.comcore.server.NotificationListener;

import java.io.File;

/**
 * Basic interface for the module class.
 */
public interface Module extends NotificationListener {

    /**
     * Returns the mdid of the module (module type)
     *
     * @return mdid of module
     */
    Mdid getMdid();

    /**
     * Prints the locator string of the module. Consists of the mdid, mnum, and groupid.
     *
     * @return String of the files location in storage
     */
    String getLocatorString();

    /**
     * Gets the group id of the module
     *
     * @return groupId of the group of the modules
     */
    String getGroupIdString();

    /**
     * Sets the muted status of the module
     *
     * @param muted muted status
     */
    void setMuted(boolean muted);

    /**
     * Gets the muted status of the module
     *
     * @return muted status
     */
    boolean isMuted();

    /**
     * Caches data to cache
     */
    void toCache();

    /**
     * Retrieves data from cache
     */
    void fromCache();

}
