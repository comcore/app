package com.gmail.comcorecrew.comcore.classes;

import com.gmail.comcorecrew.comcore.server.id.UserID;

/**
 * The User class is used to store information about other users for reference by the client
 * It does not allow anyone to edit the userID or userName.
 *
 * NOTE ABOUT INTERNAL IDS:
 * Internal Ids exist so the application can store user info in an array and retrieve it
 * in O(1) time. There is an internal id for each external id. These relationships are mapped
 * through the UserStorage class. External ids are converted to user data in O(log(n)) time.
 */
public class User {

    private int internalId; //Internal id of the user
    private final UserID userID; //External userID of the user
    private final String userName; //Username of the user

    public User (UserID userID, String userName) {
        if ((userID == null) || (userName == null)) {
            throw new IllegalArgumentException();
        }
        this.userID = userID;
        this.userName = userName;
        internalId = -1; //Unregistered
    }

    public User (UserID userID, String userName, int internalId) {
        this.userID = userID;
        this.userName = userName;
        this.internalId = internalId;
    }

    public UserID getID() {
        return this.userID;
    }

    public String getName() {
        return this.userName;
    }

    public int getInternalId() {
        return internalId;
    }

    public void setInternalId(int internalId) {
        if (this.internalId == -1) {
            this.internalId = internalId;
        }
    }
}
