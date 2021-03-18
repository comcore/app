package com.gmail.comcorecrew.comcore.classes;

import com.gmail.comcorecrew.comcore.server.id.UserID;

/** The User class is used to store information about other users for reference by the client
 * It does not allow anyone to edit the userID or userName.
 */
public class User {

    private int internalId;
    private final UserID userID;
    private final String userName;

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
