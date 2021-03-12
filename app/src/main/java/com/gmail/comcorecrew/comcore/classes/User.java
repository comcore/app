package com.gmail.comcorecrew.comcore.classes;

import com.gmail.comcorecrew.comcore.server.id.UserID;

/** The User class is used to store information about other users for reference by the client
 * It does not allow anyone to edit the userID or userName.
 */
public class User {

    private UserID userID;
    private String userName;

    public User (UserID userID, String userName) {
        this.userID = userID;
        this.userName = userName;
    }

    public UserID getID() {
        return this.userID;
    }

    public String getName() {
        return this.userName;
    }
}
