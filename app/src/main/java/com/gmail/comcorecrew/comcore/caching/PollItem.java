package com.gmail.comcorecrew.comcore.caching;

import com.gmail.comcorecrew.comcore.server.entry.PollEntry;
import com.gmail.comcorecrew.comcore.server.entry.PollOption;
import com.gmail.comcorecrew.comcore.server.id.PollID;
import com.gmail.comcorecrew.comcore.server.id.PollListID;

import java.util.ArrayList;
import java.util.List;

public class PollItem implements Cacheable {

    private int userId;
    private long pollId;
    private int vote;
    String description;
    private String[] options;
    private int[] votes;

    /** resultsVisible does not need to be cached between user sessions **/
    boolean resultsVisible;

    public PollItem(PollEntry entry) {
        userId = UserStorage.getInternalId(entry.creator);
        pollId = entry.id.id;
        vote = entry.vote;
        options = new String[entry.options.size()];
        votes = new int[entry.options.size()];
        description = entry.description;
        for (int i = 0; i < entry.options.size(); i++) {
            options[i] = entry.options.get(i).description;
            votes[i] = entry.options.get(i).numberOfVotes;
        }
    }

    public int getUserId() {
        return userId;
    }

    public long getPollId() {
        return pollId;
    }

    public int getVote() {
        return vote;
    }

    public String getDescription() {
        return description;
    }

    public List<PollOption> getOptions() {
        List<PollOption> polls = new ArrayList<>();

        for (int i = 0; i < options.length; i++) {
            polls.add(new PollOption(options[i], votes[i]));
        }

        return polls;
    }

    public int[] getVotes() {
        return votes;
    }

    public int getTotalVotes() {
        int totalVotes = 0;
        for (int i = 0; i < votes.length; i++) {
            totalVotes += votes[i];
        }
        return totalVotes;
    }

    public String[] getOptionDescriptions() {
        return options;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setPollId(long pollId) {
        this.pollId = pollId;
    }

    public void setVote(int vote) {
        this.vote = vote;
    }

    public boolean getResultsVisible() {
        return resultsVisible;
    }

    public void toggleResultsVisible() {
        resultsVisible = !resultsVisible;
    }

    public void setOptions(List<PollOption> options) {
        this.options = new String[options.size()];
        votes = new int[options.size()];
        for (int i = 0; i < options.size(); i++) {
            this.options[i] = options.get(i).description;
            votes[i] = options.get(i).numberOfVotes;
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int charLength() {
        int total = minLength();
        total += description.length(); //Description
        for (String option : options) {
            total += 2 + 2 + option.length(); //Options
        }
        return total;
    }

    public static int minLength() {
        int total = 0;
        total += 2; //userId
        total += 4; //pollId
        total += 2; //vote
        total += 2; //description length
        total += 2; //options length
        return total;
    }

    @Override
    public char[] toCache() {
        char[] cache = new char[charLength()];
        int index = 0;
        int length;

        cache[index++] = (char) (userId >> 16);
        cache[index++] = (char) userId;
        cache[index++] = (char) (pollId >> 48);
        cache[index++] = (char) (pollId >> 32);
        cache[index++] = (char) (pollId >> 16);
        cache[index++] = (char) pollId;
        cache[index++] = (char) (vote >> 16);
        cache[index++] = (char) vote;
        length = description.length();
        cache[index++] = (char) (length >> 16);
        cache[index++] = (char) length;
        for (int i = 0; i < length; i++) {
            cache[index++] = description.charAt(i);
        }
        length = options.length;
        cache[index++] = (char) (length >> 16);
        cache[index++] = (char) length;
        for (int j = 0; j < options.length; j++) {
            cache[index++] = (char) (votes[j] >> 16);
            cache[index++] = (char) votes[j];
            length = options[j].length();
            cache[index++] = (char) (length >> 16);
            cache[index++] = (char) length;
            for (int i = 0; i < length; i++) {
                cache[index++] = options[j].charAt(i);
            }
        }
        return cache;
    }

    public PollEntry toEntry(PollListID listID) {
        return new PollEntry(new PollID(listID, pollId), UserStorage.getUser(userId).getID(),
                description, getOptions(), vote);
    }

    public PollItem(char[] cache) {
        if (cache.length < minLength()) { //Min size
            throw new IllegalArgumentException("Invalid Poll Item length");
        }
        int index = 0;
        int length;

        userId = cache[index++];
        userId = (userId << 16) | cache[index++];
        pollId = cache[index++];
        pollId = (pollId << 16) | cache[index++];
        pollId = (pollId << 16) | cache[index++];
        pollId = (pollId << 16) | cache[index++];
        vote = cache[index++];
        vote = (vote << 16) | cache[index++];
        length = cache[index++];
        length = (length << 16) | cache[index++];
        description = String.copyValueOf(cache, index, length);
        index += length;
        length = cache[index++];
        length = (length << 16) | cache[index++];
        votes = new int[length];
        options = new String[length];
        for (int i = 0; index < cache.length; i++) {
            votes[i] = cache[index++];
            votes[i] = (votes[i] << 16) | cache[index++];
            length = cache[index++];
            length = (length << 16) | cache[index++];
            options[i] = String.copyValueOf(cache, index, length);
            index += length;
        }
    }
}
