package no.nemeas.numbers;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

// the game should be divided into stages and lvls, where one lvl contains an infinity number of stages.
// when starting a lvl the timer begins, and the timer is always, lets say 1 minute. - done
// U get points based on how many stages u can do in a minute, and how many mistakes u make. (1 win = 10 points, 1 loss = -4 points?) - needs tweaking no matter what..

// Each successful stage results in a quick "thumbs up", followed by a new stage. - done
// If u press the wrong number, a quick feedback is given, and a new stage begins. - done
// until the timer runs out. - done
// when u finish a lvl we want the player to continue playing, so it should be easy to go to the next lvl.
// after finishing a lvl, this is a natural spot for ads..

// needs something to route u into playing the game; like: start game at lvl 12.

// needs to increase difficulty based on lvl.

public class GameActivity extends AppCompatActivity {

    private Ad ad;
    private static Timer timer;
    private ImageButton mNextLevelButton;

    private GameState state = new GameState();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        ad = new Ad(this);

        mNextLevelButton = ((ImageButton) findViewById(R.id.nextLvl));
        mNextLevelButton.setVisibility(View.INVISIBLE);
        mNextLevelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ad.showAd();
            }
        });

        setupStage();
        startTimer();
    }

    private void startTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                setGameStateTimeOut();
            }
        }, Settings.lvlDuration * 1000);
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
        showNextLvlButton();

        // show stats
    }

    private void showNextLvlButton() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mNextLevelButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void hideNextLvlButton() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mNextLevelButton.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void setGameStateStageFailed() {
        state.failStage();
        showThumbsDown();
        setupStage();
        // TODO - implement timing of each stage/lvl
    }

    public void nextLvl() {
        // prepare for the next lvl
        hideNextLvlButton();

        ad.doStuff();

        // to other stuff as well
        state.nextLvl();
        // TODO - init new view

        setupStage();

        startTimer();
    }
}
