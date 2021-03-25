package com.gmail.comcorecrew.comcore.enums;

public enum Mdid {

    TEST, //TESTING ONLY
    CSTM, //Custom Module
    CMSG; //Comcore Messaging

    public static Mdid fromString(String mdid) {
        switch (mdid) {
            case "test":
                return TEST;
            case "cstm":
                return CMSG;
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
            case CSTM:
                return "cstm";
            case CMSG:
                return "cmsg";
            default:
                throw new IllegalStateException("Invalid mdid");
        }
    }
}
