package com.gmail.comcorecrew.comcore.classes.modules;

import com.gmail.comcorecrew.comcore.abstracts.Module;
import com.gmail.comcorecrew.comcore.caching.Cacheable;
import com.gmail.comcorecrew.comcore.caching.Cacher;
import com.gmail.comcorecrew.comcore.caching.PollItem;
import com.gmail.comcorecrew.comcore.caching.TaskItem;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.enums.Mdid;
import com.gmail.comcorecrew.comcore.notifications.NotificationScheduler;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.PollEntry;
import com.gmail.comcorecrew.comcore.server.entry.TaskEntry;
import com.gmail.comcorecrew.comcore.server.id.CalendarID;
import com.gmail.comcorecrew.comcore.server.id.ItemID;
import com.gmail.comcorecrew.comcore.server.id.PollID;
import com.gmail.comcorecrew.comcore.server.id.PollListID;
import com.gmail.comcorecrew.comcore.server.id.TaskID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Polling extends Module {

    private transient ArrayList<PollItem> polls;

    public Polling(String name, PollListID pollListID, Group group) {
        super(name, pollListID, group, Mdid.CPLS);
        polls = new ArrayList<>();
    }

    public Polling(String name, Group group) {
        super(name, group, Mdid.CPLS);
        polls = new ArrayList<>();
    }

    public ArrayList<PollItem> getPolls() {
        return polls;
    }

    public void setPolls(ArrayList<PollItem> polls) {
        this.polls = polls;
    }

    /**
     * Gets the polls in a poll entry format
     *
     * @return  a list of PollEntries contained in the module
     */
    public List<PollEntry> getEntries() {
        ArrayList<PollEntry> entries = new ArrayList<>();
        for (PollItem poll : polls) {
            entries.add(poll.toEntry((PollListID) getId()));
        }
        return entries;
    }

    /**
     * Creates a new poll to the server
     *
     * @param description   the question of the poll
     * @param options       the options to chose from
     */
    public void sendPoll(String description, String[] options) {
        ServerConnector.addPoll((PollListID) getId(), description, Arrays.asList(options), result -> {
            if (result.isFailure()) {
                return;
            }

            polls.add(new PollItem(result.data));
            toCache();
        });
    }

    /**
     * Casts a vote on the given poll
     *
     * @param pollID    Poll to vote on
     * @param choice    index of the choice
     */
    public void votePoll( PollID pollID, int choice) {
        PollItem poll = getPoll(pollID);
        if (poll != null) {
            ServerConnector.voteOnPoll(pollID, choice, result -> {
                if (result.isFailure()) {
                    ErrorDialog.show(result.errorMessage);
                    return;
                }

                poll.setVote(choice);
                toCache();
            });
        }
    }

    /**
     * Returns the PollItem associated with the given pollId
     *
     * @param pollID    pollId of the poll
     * @return          PollItem with the given pollId
     */
    public PollItem getPoll(PollID pollID) {
        for (PollItem poll : polls) {
            if (poll.getPollId() == pollID.id) {
                return poll;
            }
        }
        return null;
    }

    /**
     * Gets the results of a poll
     *
     * @param pollID    requested poll
     * @return          an array of integers containing the results
     */
    public int[] getResults(PollID pollID) {
        return getPoll(pollID).getVotes();
    }

    public void readToCache() {
        if (polls.isEmpty()) {
            return;
        }
        Cacher.cacheData(new ArrayList<>(polls), this);
    }

    public void readFromCache() {
        polls = new ArrayList<>();

        char[][] data = Cacher.uncacheData(this);
        if (data == null) {
            return;
        }

        for (char[] line : data) {
            polls.add(new PollItem(line));
        }
    }

    public void refresh() {
        ServerConnector.getPolls((PollListID) getId(), result ->  {
            if (result.isFailure()) {
                return;
            }
            polls = new ArrayList<>();
            for (PollEntry entry : result.data) {
                polls.add(new PollItem(entry));
            }
            toCache();
        });
    }

}
