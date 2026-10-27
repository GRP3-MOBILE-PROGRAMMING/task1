package com.example.buildgradle;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkChecker {

    // Lightweight endpoint that returns HTTP 204 with an empty body.
    // Later you can swap this for the UNEB server's health endpoint.
    private static final String PROBE_URL = "https://www.google.com/generate_204";
    private static final int TIMEOUT_MS = 4000;

    private final Context context;

    public NetworkChecker(Context context) {
        this.context = context.getApplicationContext();
    }

    /** Quick check: does the device report an active network with internet capability? */
    public boolean hasNetwork() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        Network network = cm.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities caps = cm.getNetworkCapabilities(network);
        return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    /** Real check: can we actually reach the internet? MUST be called off the main thread. */
    public boolean canReachInternet() {
        if (!hasNetwork()) return false;

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(PROBE_URL).openConnection();
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setUseCaches(false);
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            return code == 204 || code == 200;
        } catch (Exception e) {
            return false;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}