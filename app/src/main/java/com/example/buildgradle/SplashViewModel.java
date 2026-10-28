package com.example.buildgradle;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.util.Log;

/**
 * Runs the splash loading steps in the background and exposes their progress as LiveData.
 */
public class SplashViewModel extends AndroidViewModel {

    /** Overall state of the loading sequence. */
    public enum Phase { LOADING, READY, ERROR }

    private static final int WORKER_THREAD_COUNT = 4;
    /** Short pause per step so the user can see the progress. */
    private static final long STEP_DELAY_MS = 600;

    private final ExecutorService executor = Executors.newFixedThreadPool(WORKER_THREAD_COUNT);
    private final NetworkChecker networkChecker;
    private final MutableLiveData<StepStatus> internetStep =
            new MutableLiveData<>(StepStatus.PENDING);
    private final MutableLiveData<StepStatus> timeStep = new MutableLiveData<>(StepStatus.PENDING);
    private final MutableLiveData<StepStatus> authStep = new MutableLiveData<>(StepStatus.PENDING);
    private final MutableLiveData<StepStatus> dataStep = new MutableLiveData<>(StepStatus.PENDING);

    private final MutableLiveData<Phase> phase = new MutableLiveData<>(Phase.LOADING);
    private final MutableLiveData<SplashError> error = new MutableLiveData<>();

    private boolean started = false;

    private final TimeValidator timeValidator = new TimeValidator();

    /**
     * Creates the view model. Called by the ViewModelProvider.
     *
     * @param application the running application
     */
    public SplashViewModel(@NonNull Application application) {
        super(application);
        networkChecker = new NetworkChecker(application);
    }

    /**
     * Status of the internet check step.
     *
     * @return the internet step status
     */
    public LiveData<StepStatus> getInternetStep() {
        return internetStep;
    }

    /**
     * Status of the secure time validation step.
     *
     * @return the time step status
     */
    public LiveData<StepStatus> getTimeStep() {
        return timeStep;
    }

    /**
     * Status of the authentication token prefetch step.
     *
     * @return the auth step status
     */
    public LiveData<StepStatus> getAuthStep() {
        return authStep;
    }

    /**
     * Status of the years and announcement download step.
     *
     * @return the data step status
     */
    public LiveData<StepStatus> getDataStep() {
        return dataStep;
    }

    /**
     * Overall loading phase.
     *
     * @return the current phase
     */
    public LiveData<Phase> getPhase() {
        return phase;
    }

    /**
     * Error that stopped loading, or null if none.
     *
     * @return the current error
     */
    public LiveData<SplashError> getError() {
        return error;
    }

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
                Thread.sleep(STEP_DELAY_MS);
                if (!networkChecker.canReachInternet()) {
                    internetStep.postValue(StepStatus.FAILED);
                    error.postValue(SplashError.NO_INTERNET);
                    phase.postValue(Phase.ERROR);
                    return;
                }
                internetStep.postValue(StepStatus.SUCCESS);

                // TODO Step 5: replace with the real NTP time validation
                timeStep.postValue(StepStatus.RUNNING);
                Thread.sleep(STEP_DELAY_MS);
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
                Thread.sleep(STEP_DELAY_MS);
                authStep.postValue(StepStatus.SUCCESS);

                // TODO Step 7: replace with the real announcement download
                dataStep.postValue(StepStatus.RUNNING);
                Thread.sleep(STEP_DELAY_MS);
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
