package no.nemeas.numbers;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
// needs a countdown before a lvl starts

// needs to increase difficulty based on lvl.

public class GameActivity extends AppCompatActivity {

    private Ad ad;
    private static CountDownTimer timer;
    private GameState state = new GameState();
    private TextView textTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        textTimer = (TextView)findViewById(R.id.textTimer);

        ad = new Ad(this);

        nextLvl();
    }

    private void setupStage() {

        state.newStage();

        ArrayList<Button> buttons = new ArrayList<>();

        for (final int number : state.getStageNumbers()) {
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
                        setGameStateStageFailed();
                    }

                    if (state.isStageComplete()) setGameStateStageComplete();
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
        // TODO - something fishy about the display width and height, if possible, this should be changed to use the actual relative layout in the view.

        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();

        display.getSize(point);
        int width = point.x;
        int height = point.y;

        int buttonSize = width / 7;

        int centeringMarginWidth = width / 4;
        int centeringMarginHeight = height / 4;

        int margin = 20;

        Position[] positions = PositionHelper.getPositions(
                centeringMarginWidth,
                width - centeringMarginWidth,
                centeringMarginHeight,
                height - centeringMarginHeight,
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

    private void setGameStateStageComplete() {
        state.completeStage();

        showThumbsUp();

        setupStage();
    }

    private void showThumbsUp() {
        final ImageView thumbsUp = (ImageView) findViewById(R.id.thumbsUp);
        animateImage(thumbsUp);
    }

    private void showThumbsDown() {
        final ImageView thumbsDown = (ImageView) findViewById(R.id.thumbsDown);
        animateImage(thumbsDown);
    }

    private void animateImage(final ImageView image) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(1f, 0f);
        valueAnimator.setInterpolator(new AccelerateInterpolator()); // increase the speed first and then decrease
        valueAnimator.setDuration(500);
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
        state.timeOut();
        showNextLvlDialog();

        // show stats
    }

    private void showNextLvlDialog() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setMessage("wins: " + state.completedStages + "\nlosses: " + state.failedStages).setTitle("GJ");

                builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d("Dialog", "Continue");
                        // User clicked OK button
                        ad.showAd();
                    }
                });
                builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d("Dialog", "Back");
                        // do something else, go back to splash?
                        // User cancelled the dialog
                    }
                });

                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();

                dialog.show();
            }
        });
    }

    public void startTimer() {

        timer = new CountDownTimer(Settings.DURATION_OF_LVL_IN_SECS, 1000) {

            public void onTick(long millisUntilFinished) {
                long remainedSecs = millisUntilFinished / 1000;
                textTimer.setText("" + (remainedSecs / 60) + ":" + Utils.formatSecs(remainedSecs % 60));
            }

            public void onFinish() {
                textTimer.setText("0:00");
                setGameStateTimeOut();
                cancel();
            }
        }.start();
    }

    private Context getActivity() {
        return this;
    }

    private void setGameStateStageFailed() {
        state.failStage();
        showThumbsDown();
        setupStage();
        // TODO - implement timing of each stage
    }

    public void nextLvl() {
        // prepare for the next lvl
        ad.loadNewAd();

        // to other stuff as well
        state.nextLvl();

        hideBoard();

        setupStage();

        hideTimer();

        showCountDown();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showBoard();
                showTimer();
                startTimer();
            }
        }, 3600);
    }

    private void hideTimer() {
        textTimer.setVisibility(View.INVISIBLE);
    }

    private void showTimer() {
        textTimer.setVisibility(View.VISIBLE);
    }

    private void hideBoard() {
        RelativeLayout board = (RelativeLayout) findViewById(R.id.board);
        board.setVisibility(View.INVISIBLE);
    }

    private void showCountDown() {
        // TODO - implement
        TextView readyText = (TextView) findViewById(R.id.readyText);

        animateTextView(readyText, 0, 1200);

        TextView setText = (TextView) findViewById(R.id.setText);

        animateTextView (setText, 1200, 1200);

        TextView goText = (TextView) findViewById(R.id.goText);

        animateTextView(goText, 2400, 1200);
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
        RelativeLayout board = (RelativeLayout) findViewById(R.id.board);
        board.setVisibility(View.VISIBLE);
    }
}
