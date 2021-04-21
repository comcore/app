package com.gmail.comcorecrew.comcore.classes.modules;

import com.gmail.comcorecrew.comcore.abstracts.Module;
import com.gmail.comcorecrew.comcore.caching.PollItem;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.enums.Mdid;
import com.gmail.comcorecrew.comcore.server.id.CalendarID;
import com.gmail.comcorecrew.comcore.server.id.ItemID;

import java.util.ArrayList;

public class Polling extends Module {

    private transient ArrayList<PollItem> polls;

    /** resultsVisible does not need to be cached between user sessions **/
    boolean resultsVisible;

    public Polling(String name, CalendarID calendarID, Group group) {
        super(name, calendarID, group, Mdid.CPLS);
        polls = new ArrayList<>();
    }

    public Polling(String name, Group group) {
        super(name, null, group, Mdid.CPLS);
        polls = new ArrayList<>();
    }

    public ArrayList<PollItem> getPolls() {
        return polls;
    }

    public void setPolls(ArrayList<PollItem> polls) {
        this.polls = polls;
    }

    public void sendPoll(String description, String[] options) {
        //TODO Implement
    }

    public void votePoll(ItemID pollId, int choice) {
        //TODO Implement
    }

    public void deletePoll(ItemID pollId) {
        //TODO Implement
    }

    public void addPoll(Object poll) {
        //TODO Implement
    }

    public void addPolls(ArrayList<Object> polls) {
        //TODO Implement
    }

    public int[] getPollVotes(Object poll) {
        //TODO Implement
        return null;
    }

    public boolean getResultsVisible() {
        return resultsVisible;
    }

    public void toggleResultsVisible() {
        resultsVisible = !resultsVisible;
    }

    public void readToCache() {
        //TODO Implement
    }

    public void readFromCache() {
        //TODO Implement
    }

    public void refresh() {
        //TODO Implmement
    }

}
