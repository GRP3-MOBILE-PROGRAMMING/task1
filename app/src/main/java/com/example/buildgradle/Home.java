package com.example.buildgradle;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Home extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set status bar to white with dark icons
        getWindow().setStatusBarColor(android.graphics.Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        showScreen1();
    }

    // Screen 1: Splash
    private void showScreen1() {
        setContentView(R.layout.activity_main);
        handler.postDelayed(this::showScreen2, 2500);
    }

    // Screen 2: Loading and checking
    private void showScreen2() {
        setContentView(R.layout.activity_loading);
        handler.postDelayed(() -> {
            if (isInternetAvailable()) {
                showScreen4();
            } else {
                showScreen3();
            }
        }, 5000);
    }

    // Screen 3: Error
    private void showScreen3() {
        setContentView(R.layout.activity_error);
        Button retryButton = findViewById(R.id.retryButton);
        if (retryButton != null) {
            retryButton.setOnClickListener(v -> showScreen2());
        }
    }

    // Screen 4: Login
    private void showScreen4() {
        setContentView(R.layout.activity_login);
    }

    // Check internet connection
    private boolean isInternetAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) return false;

        Network network = cm.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
