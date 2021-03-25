package com.gmail.comcorecrew.comcore.interfaces;

import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.enums.Mdid;
import com.gmail.comcorecrew.comcore.notifications.NotificationListener;

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
     * Returns the module number (Distinguishes same modules apart)
     *
     * @return module number of module
     */
    int getMnum();

    /**
     * Gets the group id of the module
     *
     * @return groupId of the group of the modules
     */
    String getGroupId();

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
    boolean getMuted();

    /**
     * Caches data to cache
     */
    void toCache();

    /**
     * Retrieves data from cache
     */
    void fromCache();

    /**
     * Saves data to file
     */
    void toFile();

    /**
     * Retrieves data from file
     *
     * @param file File to read from
     */
    void fromFile(File file, Group group);
}
