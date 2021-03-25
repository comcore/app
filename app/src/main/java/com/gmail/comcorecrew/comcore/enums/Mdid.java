package com.gmail.comcorecrew.comcore.enums;

public enum Mdid {

    TEST, //TESTING ONLY
    CMSG; //Comcore Messaging

    public static Mdid fromString(String mdid) {
        switch (mdid) {
            case "test":
                return TEST;
            case "cmsg":
                return CMSG;
            default:
                throw new IllegalArgumentException("Invalid mdid: " + mdid);
        }
    }

    public String toString() {
        switch (this) {
            case TEST:
                return "test";
            case CMSG:
                return "cmsg";
            default:
                throw new IllegalStateException("Invalid mdid");
        }
    }
}
