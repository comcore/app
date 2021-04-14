package com.gmail.comcorecrew.comcore.enums;

public enum Mdid {

    TEST, //TESTING ONLY
    CSTM, //Custom Module
    CMSG, //Comcore Messaging
    CTSK, //Comcore Tasks
    CCLD; //Comcore Calendar

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
            case "ccld":
                return CCLD;
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
            case CCLD:
                return "ccld";
            default:
                throw new IllegalStateException("Invalid mdid");
        }
    }
}
