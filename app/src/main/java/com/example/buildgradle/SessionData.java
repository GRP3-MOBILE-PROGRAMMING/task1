package com.example.buildgradle;

public class SessionData {

    private static final SessionData INSTANCE = new SessionData();

    public static SessionData get() { return INSTANCE; }

    private volatile long serverOffsetMs = 0;
    private volatile boolean resultsReleased = false;

    public void setTimeInfo(long offsetMs, boolean released) {
        this.serverOffsetMs = offsetMs;
        this.resultsReleased = released;
    }

    public boolean areResultsReleased() { return resultsReleased; }

    /** Real current time, corrected using the server offset. */
    public long trustedNowMillis() { return System.currentTimeMillis() + serverOffsetMs; }
}
