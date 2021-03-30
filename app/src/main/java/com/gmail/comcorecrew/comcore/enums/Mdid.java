package com.gmail.comcorecrew.comcore.enums;

public enum Mdid {

    TEST, //TESTING ONLY
    CSTM, //Custom Module
    CMSG, //Comcore Messaging
    CTSK; //Comcore Tasks

    public static Mdid fromString(String mdid) {
        switch (mdid) {
            case "test":
                return TEST;
            case "cstm":
                return CSTM;
            case "cmsg":
                return CMSG;
            case "ctsk":
                return CTSK;
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
            case CTSK:
                return "ctsk";
            default:
                throw new IllegalStateException("Invalid mdid");
        }
    }
}
