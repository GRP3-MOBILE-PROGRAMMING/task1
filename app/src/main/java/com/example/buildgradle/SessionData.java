package com.example.buildgradle;

/**
 * Process-wide store for values the splash screen fetched, such as the trusted time offset.
 */
public class SessionData {

    private static final SessionData INSTANCE = new SessionData();

    /**
     * Returns the single shared instance.
     *
     * @return the shared session data
     */
    public static SessionData get() {
        return INSTANCE;
    }

    private volatile long serverOffsetMs = 0;
    private volatile boolean resultsReleased = false;

    /**
     * Stores the result of the time validation step.
     *
     * @param offsetMs trusted time minus device time, in milliseconds
     * @param released whether the results release time has passed
     */
    public void setTimeInfo(long offsetMs, boolean released) {
        this.serverOffsetMs = offsetMs;
        this.resultsReleased = released;
    }

    /**
     * Tells whether the results have been released, based on trusted time.
     *
     * @return true if the release time has passed
     */
    public boolean areResultsReleased() {
        return resultsReleased;
    }

    /**
     * Real current time, corrected using the server offset.
     *
     * @return the trusted current time in milliseconds since the epoch
     */
    public long trustedNowMillis() {
        return System.currentTimeMillis() + serverOffsetMs;
    }
}
