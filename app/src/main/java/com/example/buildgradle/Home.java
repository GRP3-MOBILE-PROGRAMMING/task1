package com.example.buildgradle;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(android.graphics.Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        );

        showScreen1();
    }

    private void showScreen1() {
        setContentView(R.layout.activity_main);
        new android.os.Handler(android.os.Looper.getMainLooper())
                .postDelayed(this::showScreen2, 2500);
    }

    private void showScreen2() {
        setContentView(R.layout.activity_loading);
        new android.os.Handler(android.os.Looper.getMainLooper())
                .postDelayed(this::showScreen3, 5000);
    }

    private void showScreen3() {
        setContentView(R.layout.activity_error);
        new android.os.Handler(android.os.Looper.getMainLooper())
                .postDelayed(this::showScreen4, 5000);
    }

    private void showScreen4() {
        setContentView(R.layout.activity_login);
    }
}
