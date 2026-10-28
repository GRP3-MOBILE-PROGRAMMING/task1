package com.example.buildgradle;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Gets trusted time from NTP (with an HTTPS Date header fallback) and checks the release time.
 */
public class TimeValidator {

    private static final String[] NTP_SERVERS = {
            "time.google.com", "pool.ntp.org", "time.cloudflare.com"
    };
    private static final int NTP_TIMEOUT_MS = 2500;
    private static final String HTTPS_FALLBACK_URL = "https://www.google.com/generate_204";
    private static final int HTTP_TIMEOUT_MS = 3000;

    private static final String RELEASE_TIME_ZONE = "Africa/Kampala";
    private static final int RELEASE_YEAR = 2026;
    private static final int RELEASE_HOUR = 10;

    /** Allowed difference between device clock and real time. */
    public static final long MAX_CLOCK_SKEW_MS = 5 * 60 * 1000;

    /** PLACEHOLDER release time. Change it to your demo date (month is 0-based: 0 = January). */
    public static final long RELEASE_TIME_MILLIS = buildReleaseTime();

    private static long buildReleaseTime() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone(RELEASE_TIME_ZONE));
        c.clear();
        c.set(RELEASE_YEAR, Calendar.JANUARY, 1, RELEASE_HOUR, 0, 0);
        return c.getTimeInMillis();
    }

    /**
     * Returns (real time - device time) in milliseconds. MUST be called off the main thread.
     *
     * @return the clock offset in milliseconds, or null if no time source could be reached
     */
    public Long fetchOffsetMs() {
        for (String server : NTP_SERVERS) {
            Long offset = queryNtp(server);
            if (offset != null) return offset;
        }
        return queryHttpsDate();
    }

    /**
     * Checks whether the results release time has passed, using trusted time.
     *
     * @param offsetMs trusted time minus device time, in milliseconds
     * @return true if the trusted current time is at or after the release time
     */
    public static boolean isReleased(long offsetMs) {
        long trustedNow = System.currentTimeMillis() + offsetMs;
        return trustedNow >= RELEASE_TIME_MILLIS;
    }

    private Long queryNtp(String server) {
        NTPUDPClient client = new NTPUDPClient();
        client.setDefaultTimeout(NTP_TIMEOUT_MS);
        try {
            client.open();
            TimeInfo info = client.getTime(InetAddress.getByName(server));
            info.computeDetails();
            return info.getOffset();
        } catch (Exception e) {
            return null;
        } finally {
            client.close();
        }
    }

    private Long queryHttpsDate() {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(HTTPS_FALLBACK_URL).openConnection();
            conn.setConnectTimeout(HTTP_TIMEOUT_MS);
            conn.setReadTimeout(HTTP_TIMEOUT_MS);
            conn.setUseCaches(false);
            conn.getResponseCode();
            long serverDate = conn.getDate(); // 0 if the header is missing
            if (serverDate == 0) return null;
            return serverDate - System.currentTimeMillis();
        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
