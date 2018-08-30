package no.nemeas.numbers.helpers;

import android.os.CountDownTimer;

public class Timer {
    public boolean started = false;
    public boolean paused = false;
    public boolean stopped = false;

    private long timeRemaining;
    private long duration;

    private CountDownTimer timer;

    private Listener mListener;

    public interface Listener {
        void onTimerUpdate(long millisUntilFinished);
        void onTimerFinished();
    }

    public Timer setListener(Listener listener) {
        mListener = listener;
        return this;
    }

    public Timer setDuration(long durationInMillis) {
        this.duration = durationInMillis;
        this.timeRemaining = durationInMillis;
        return this;
    }

    public void initialize() {
        timer = new CountDownTimer(timeRemaining, 1000) {

            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                mListener.onTimerUpdate(millisUntilFinished);
            }

            public void onFinish() {
                mListener.onTimerFinished();
            }
        };
    }

    public void start() {
        this.started = true;
        this.stopped = false;
        this.paused = false;
        if (timer == null)
            initialize();
        timer.start();
    }

    public void pause() {
        if (stopped)
            return;

        this.started = false;
        this.paused = true;
        this.stopped = false;

        if (this.timer != null)
            this.timer.cancel();
    }

    public void resume() {
        if(stopped)
            return;

        start();
    }

    public void stop() {
        this.paused = false;
        this.started = false;
        this.stopped = true;

        this.timeRemaining = duration;
        if (this.timer != null)
            this.timer.cancel();
    }
}
