package com.example.buildgradle;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.util.Log;

public class SplashViewModel extends AndroidViewModel {

    public enum Phase { LOADING, READY, ERROR }


    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final NetworkChecker networkChecker;
    private final MutableLiveData<StepStatus> internetStep = new MutableLiveData<>(StepStatus.PENDING);
    private final MutableLiveData<StepStatus> timeStep = new MutableLiveData<>(StepStatus.PENDING);
    private final MutableLiveData<StepStatus> authStep = new MutableLiveData<>(StepStatus.PENDING);
    private final MutableLiveData<StepStatus> dataStep = new MutableLiveData<>(StepStatus.PENDING);

    private final MutableLiveData<Phase> phase = new MutableLiveData<>(Phase.LOADING);
    private final MutableLiveData<SplashError> error = new MutableLiveData<>();

    private boolean started = false;

    private final TimeValidator timeValidator = new TimeValidator();

    public SplashViewModel(@NonNull Application application) {
        super(application);
        networkChecker =new NetworkChecker(application);
    }

    public LiveData<StepStatus> getInternetStep() { return internetStep; }
    public LiveData<StepStatus> getTimeStep() { return timeStep; }
    public LiveData<StepStatus> getAuthStep() { return authStep; }
    public LiveData<StepStatus> getDataStep() { return dataStep; }
    public LiveData<Phase> getPhase() { return phase; }
    public LiveData<SplashError> getError() { return error; }

    /** Called from onCreate. Does nothing if loading has already started (e.g. after rotation). */
    public void start() {
        if (started) return;
        started = true;
        run();
    }

    /** Called when the user taps Retry. */
    public void retry() {
        run();
    }

    private void run() {
        // Runs on the main thread, so setValue is safe here.
        internetStep.setValue(StepStatus.PENDING);
        timeStep.setValue(StepStatus.PENDING);
        authStep.setValue(StepStatus.PENDING);
        dataStep.setValue(StepStatus.PENDING);
        error.setValue(null);
        phase.setValue(Phase.LOADING);

        executor.execute(() -> {

            try {
                // TODO Step 4: replace with the real internet check
                internetStep.postValue(StepStatus.RUNNING);
                Thread.sleep(600);
                if (!networkChecker.canReachInternet()) {
                    internetStep.postValue(StepStatus.FAILED);
                    error.postValue(SplashError.NO_INTERNET);
                    phase.postValue(Phase.ERROR);
                    return;
                }
                internetStep.postValue(StepStatus.SUCCESS);

                // TODO Step 5: replace with the real NTP time validation
                timeStep.postValue(StepStatus.RUNNING);
                Thread.sleep(600);
                timeStep.postValue(StepStatus.SUCCESS);
                timeStep.postValue(StepStatus.RUNNING);
                Long offset = timeValidator.fetchOffsetMs();
                if (offset == null) {
                    // Could not reach any time source, so the time cannot be trusted
                    timeStep.postValue(StepStatus.FAILED);
                    error.postValue(SplashError.SERVER_UNREACHABLE);
                    phase.postValue(Phase.ERROR);
                    return;
                }
                if (Math.abs(offset) > TimeValidator.MAX_CLOCK_SKEW_MS) {
                    // The device clock is wrong (or has been changed)
                    timeStep.postValue(StepStatus.FAILED);
                    error.postValue(SplashError.TIME_INVALID);
                    phase.postValue(Phase.ERROR);
                    return;
                }
                boolean released = TimeValidator.isReleased(offset);
                SessionData.get().setTimeInfo(offset, released);
                Log.d("UNEB", "Time OK. offset=" + offset + "ms, resultsReleased=" + released);
                timeStep.postValue(StepStatus.SUCCESS);

                // TODO Step 6: replace with the real token and exam years prefetch
                authStep.postValue(StepStatus.RUNNING);
                Thread.sleep(600);
                authStep.postValue(StepStatus.SUCCESS);

                // TODO Step 7: replace with the real announcement download
                dataStep.postValue(StepStatus.RUNNING);
                Thread.sleep(600);
                dataStep.postValue(StepStatus.SUCCESS);

                phase.postValue(Phase.READY);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    @Override
    protected void onCleared() {
        executor.shutdownNow();


    }
}
