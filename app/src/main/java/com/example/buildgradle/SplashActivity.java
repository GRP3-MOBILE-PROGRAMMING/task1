package com.example.buildgradle;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

/**
 * Launcher activity: shows the splash, then the loading steps, then the error or login screen.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_MS = 1500; // Step 8 refines the minimum display time

    private static final int SCREEN_SPLASH = 0;
    private static final int SCREEN_LOADING = 1;
    private static final int SCREEN_ERROR = 2;
    private static final int SCREEN_LOGIN = 3;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private SplashViewModel viewModel;

    private int screen = SCREEN_SPLASH;
    private boolean splashDone = false;

    private TextView stepInternet, stepTime, stepAuth, stepData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        // Splash appears immediately; loading starts in the background at the same time.
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(SplashViewModel.class);
        viewModel.getInternetStep().observe(this, s -> renderSteps());
        viewModel.getTimeStep().observe(this, s -> renderSteps());
        viewModel.getAuthStep().observe(this, s -> renderSteps());
        viewModel.getDataStep().observe(this, s -> renderSteps());
        viewModel.getPhase().observe(this, p -> route());

        viewModel.start();

        handler.postDelayed(() -> {
            splashDone = true;
            route();
        }, SPLASH_MS);
    }

    /** Decides which screen to show based on the loading phase. */
    private void route() {
        if (!splashDone) return;
        SplashViewModel.Phase phase = viewModel.getPhase().getValue();
        if (phase == null) return;

        switch (phase) {
            case LOADING:
                showLoading();
                break;
            case ERROR:
                showError();
                break;
            case READY:
                goNext();
                break;
        }
    }

    private void showLoading() {
        if (screen == SCREEN_LOADING) return;
        screen = SCREEN_LOADING;
        setContentView(R.layout.activity_loading);
        stepInternet = findViewById(R.id.stepInternet);
        stepTime = findViewById(R.id.stepTime);
        stepAuth = findViewById(R.id.stepAuth);
        stepData = findViewById(R.id.stepData);
        renderSteps();
    }

    private void showError() {
        if (screen == SCREEN_ERROR) return;
        screen = SCREEN_ERROR;
        setContentView(R.layout.activity_error);

        TextView message = findViewById(R.id.errorMessage);
        SplashError error = viewModel.getError().getValue();
        if (error == SplashError.NO_INTERNET) {
            message.setText("No internet connection. Check your network and try again.");
        } else if (error == SplashError.TIME_INVALID) {
            message.setText("Your device time doesn't match the secure server time. "
                    + "Correct your date and time settings and try again.");
        } else if (error == SplashError.SERVER_UNREACHABLE) {
            message.setText("We couldn't reach the UNEB servers. Please try again.");
        }

        Button retry = findViewById(R.id.retryButton);
        retry.setOnClickListener(v -> {
            viewModel.retry();
            showLoading();
        });
    }

    /**
     * Temporary: Step 9 replaces this with a real transition to
     * LoginActivity/DashboardActivity.
     */
    private void goNext() {
        if (screen == SCREEN_LOGIN) return;
        screen = SCREEN_LOGIN;
        setContentView(R.layout.activity_login);
    }

    private void renderSteps() {
        if (stepInternet == null) return; // loading screen not showing yet
        setStep(stepInternet, "Checking internet connection",
                viewModel.getInternetStep().getValue());
        setStep(stepTime, "Validating secure time", viewModel.getTimeStep().getValue());
        setStep(stepAuth, "Preparing authentication", viewModel.getAuthStep().getValue());
        setStep(stepData, "Loading years and announcement", viewModel.getDataStep().getValue());
    }

    private void setStep(TextView view, String label, StepStatus status) {
        if (status == null) status = StepStatus.PENDING;
        switch (status) {
            case PENDING:
                view.setText("○ " + label);
                view.setTextColor(Color.parseColor("#777777"));
                break;
            case RUNNING:
                view.setText("… " + label);
                view.setTextColor(Color.parseColor("#222222"));
                break;
            case SUCCESS:
                view.setText("✓ " + label);
                view.setTextColor(Color.parseColor("#2E7D32"));
                break;
            case FAILED:
                view.setText("✗ " + label);
                view.setTextColor(Color.parseColor("#B00020"));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
