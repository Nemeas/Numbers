package no.nemeas.numbers;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.games.Games;

import java.util.ArrayList;

// the game should be divided into stages and lvls, where one lvl contains an infinity number of stages. - done
// when starting a lvl the timer begins, and the timer is always, lets say 1 minute. - done
// U get points based on how many stages u can do in a minute, and how many mistakes u make. (1 win = 10 points, 1 loss = -4 points?) - needs tweaking no matter what..

// Each successful stage results in a quick "thumbs up", followed by a new stage. - done
// If u press the wrong number, a quick feedback is given, and a new stage begins. - done
// until the timer runs out. - done
// when u finish a lvl we want the player to continue playing, so it should be easy to go to the next lvl. - done
// after finishing a lvl, this is a natural spot for ads.. - done

// needs something to route u into playing the game; like: start game at lvl 12.
// needs a countdown before a lvl starts - done

// needs to increase difficulty based on lvl.

public class GameActivity extends Activity {

    private Ad ad;
    private GameState state = new GameState();
    private TextView textTimer;
    private Timer timer = new Timer(this);
    private boolean stageComplete = false;
    // private FirebaseAnalytics mFirebaseAnalytics;

    public static final String DEBUG = "DeBuG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(DEBUG, "onCreate");
        setContentView(R.layout.activity_game);

        state.lvl = getIntent().getIntExtra("lvl", 1);

        textTimer = (TextView)findViewById(R.id.textTimer);

        ad = new Ad(this);

        setupGamePremise();

        nextStage();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.timer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(this.stageComplete)
            return;

        if(this.timer.paused)
            this.timer.resume();
    }

    private void setupGamePremise() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//Set Portrait
    }

    private void saveNewHighscore(int score) {
        Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
            .submitScore(getString(R.string.leaderboard_highscore), score);
    }

    private void setupNewRound() {
        Log.d(DEBUG, "setupNewRound");

        state.newRound();

        ArrayList<Button> buttons = new ArrayList<>();

        for (final int number : state.getRoundNumbers()) {
            Button b = new Button(this);
            b.setBackgroundColor(Utils.getRandomBackgroundColor());
            b.setText(number + "");
            b.setId(number);

            b.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    if (state.state == StateEnum.timeout) return;

                    // Perform action on click
                    Button b = (Button) findViewById(number);
                    if (state.isCorrect(number)) {
                        b.setBackgroundColor(Color.GREEN);
                        state.removeNumber(number);
                    } else {
                        b.setBackgroundColor(Color.RED);
                        setGameStateRoundFailed();
                    }

                    if (state.isRoundComplete()) setGameStateRoundComplete();
                }
            });
            buttons.add(b);
        }

        positionButtons(buttons);

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.board);

        relativeLayout.removeAllViewsInLayout();

        for (Button button : buttons) {
            relativeLayout.addView(button);
        }
    }

    private void positionButtons(ArrayList<Button> buttons) {
        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();

        display.getSize(point);
        int width = point.x;
        int height = point.y;

        int buttonSize = width / Settings.BUTTON_RELATIVE_SIZE;

        int centeringMarginWidth = width / Settings.GAME_RELATIVE_MARGIN;
        int centeringMarginHeight = height / Settings.GAME_RELATIVE_MARGIN;

        int margin = Settings.BUTTON_MARGIN;

        Position[] positions = PositionHelper.getPositions(
                centeringMarginWidth - buttonSize / 2,
                width - centeringMarginWidth - buttonSize / 2,
                centeringMarginHeight - buttonSize / 2,
                height - centeringMarginHeight - buttonSize / 2,
                buttons.size(),
                buttonSize,
                margin);

        for (int i = 0 ; i < positions.length ; i++) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(buttonSize, buttonSize);

            layoutParams.topMargin = positions[i].y;
            layoutParams.leftMargin = positions[i].x;
            buttons.get(i).setLayoutParams(layoutParams);
        }
    }

    private void setGameStateRoundComplete() {
        state.completeRound();

        showThumbsUp();

        setupNewRound();
    }

    private void showThumbsUp() {
        final ImageView thumbsUp = (ImageView) findViewById(R.id.thumbsUp);
        animateImage(thumbsUp, Settings.SUCCESS_IMAGE_DURATION);
    }

    private void showThumbsDown() {
        final ImageView thumbsDown = (ImageView) findViewById(R.id.thumbsDown);
        animateImage(thumbsDown, Settings.FAILURE_IMAGE_DURATION);
    }

    private void animateImage(final ImageView image, int duration) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(1f, 0f);
        valueAnimator.setInterpolator(new AccelerateInterpolator()); // increase the speed first and then decrease
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                image.setAlpha(progress);
            }
        });
        valueAnimator.start();
    }

    private void setGameStateTimeOut() {
        Log.d(DEBUG, "setGameStateTimeOut");

        state.timeOut();
        timer.stop();
        this.stageComplete = true;
        saveNewHighscore(state.completedRounds - state.failedRounds);
        showNextStageDialog();
    }

    private void showNextStageDialog() {
        Log.d(DEBUG, "showNextStageDialog");

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                // 2. Chain together various setter methods to set the dialog characteristics
                String title = getCompleteTitle(state.completedRounds - state.failedRounds);
                builder.setMessage("Awsomeness: " + state.completedRounds + "\nFails: " + state.failedRounds).setTitle(title);

                builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d(DEBUG, "showNextStageDialog.Continue");
                        // User clicked OK button
                        ad.showAd();
                    }
                });

                builder.setCancelable(false);

                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();

                dialog.show();
            }
        });
    }

    private String getCompleteTitle(int score) {
        if (score > 35)
            return "Un freakin believable!";
        if (score > 30)
            return "Awsome!";
        if (score > 25)
            return "Wow! Keep up the good work!";
        if (score > 20)
            return "Gj!";
        if (score > 15)
            return "Nice";
        if (score > 10)
            return "Come on, you can do better!";
        if (score > 5)
            return "Well well..";
        return "Try again later..";
    }

    private Context getActivity() {
        return this;
    }

    private void setGameStateRoundFailed() {
        state.failRound();
        showThumbsDown();
        setupNewRound();
        // TODO - implement timing of each round?
    }

    public void nextStage() {
        Log.d(DEBUG, "nextStage");

        this.stageComplete = false;

        // prepare for the next stage
        ad.loadNewAd();

        // to other stuff as well
        state.nextStage();

        hideBoard();

        setupNewRound();

        hideTimer();

        showCountDown();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showBoard();
                showTimer();
                ((GameActivity)getActivity()).timer.start();
            }
        }, Settings.DURATION_OF_COUNTDOWN_IN_MILLI_SECS);
    }

    private void hideTimer() {
        textTimer.setVisibility(View.INVISIBLE);
        Log.d(DEBUG, "hideTimer");
    }

    private void showTimer() {
        textTimer.setVisibility(View.VISIBLE);
        Log.d(DEBUG, "showTimer");
    }

    private void hideBoard() {
        RelativeLayout board = (RelativeLayout) findViewById(R.id.board);
        board.setVisibility(View.INVISIBLE);
        Log.d(DEBUG, "hideBoard");
    }

    private void showCountDown() {
        Log.d(DEBUG, "showCountDown");

        int duration = Settings.DURATION_OF_COUNTDOWN_IN_MILLI_SECS / 3;

        TextView readyText = (TextView) findViewById(R.id.readyText);
        animateTextView(readyText, duration * 0, duration);

        TextView setText = (TextView) findViewById(R.id.setText);
        animateTextView (setText, duration * 1, duration);

        TextView goText = (TextView) findViewById(R.id.goText);
        animateTextView(goText, duration * 2, duration);
    }

    private void animateTextView(final TextView view, long delay, long duration) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(1f, 0f);
        valueAnimator.setInterpolator(new AccelerateInterpolator()); // increase the speed first and then decrease
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                view.setAlpha(progress);
            }

        });

        valueAnimator.setStartDelay(delay);

        valueAnimator.start();
    }

    private void showBoard() {
        Log.d(DEBUG, "showBoard");

        RelativeLayout board = (RelativeLayout) findViewById(R.id.board);
        board.setVisibility(View.VISIBLE);
    }

    public void onTimerUpdate(long millisRemaining) {
        long secs = millisRemaining / 1000;
        textTimer.setText("" + (secs / 60) + ":" + Utils.formatSecs(secs % 60));
    }

    public void onTimerFinished() {
        Log.d(DEBUG, "onTimerFinished");
        textTimer.setText("0:00");
        setGameStateTimeOut();
    }
}
