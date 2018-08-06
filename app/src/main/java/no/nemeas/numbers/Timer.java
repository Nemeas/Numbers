package no.nemeas.numbers;

import android.os.CountDownTimer;
import android.util.Log;


public class Timer {
    public boolean started = false;
    public boolean paused = false;
    public boolean stopped = false;

    private long timeRemaining = Settings.DURATION_OF_LVL_IN_MILLI_SECS;

    private CountDownTimer timer;

    private GameActivity gameActivity;

    public Timer(GameActivity ga) {
        this.gameActivity = ga;
    }

    public void start() {
        Log.d(GameActivity.DEBUG, "timer.start()");

        this.started = true;
        this.stopped = false;
        this.paused = false;
        timer = new CountDownTimer(timeRemaining, 1000) {

            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                gameActivity.onTimerUpdate(millisUntilFinished);
            }

            public void onFinish() {
                gameActivity.onTimerFinished();
            }
        }.start();
    }

    public void pause() {
        Log.d(GameActivity.DEBUG, "timer.pause()");

        if (stopped)
            return;

        this.started = false;
        this.paused = true;
        this.stopped = false;

        this.timer.cancel();
    }

    public void resume() {
        Log.d(GameActivity.DEBUG, "timer.resume()");

        if(stopped)
            return;

        start();
    }

    public void stop() {
        Log.d(GameActivity.DEBUG, "timer.stop()");

        this.paused = false;
        this.started = false;
        this.stopped = true;

        this.timeRemaining = Settings.DURATION_OF_LVL_IN_MILLI_SECS;
        this.timer.cancel();
    }
}
