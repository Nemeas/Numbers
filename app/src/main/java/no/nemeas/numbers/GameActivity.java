package no.nemeas.numbers;

import android.animation.ValueAnimator;
import android.graphics.Color;
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
import java.util.Random;
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

    private InterstitialAd mInterstitialAd;

    private static int[] numbers;
    private static StateEnum state = StateEnum.initial;
    private static int completedStages = 0;
    private static int failedStages = 0;
    private static Timer timer;
    private ImageButton mNextLevelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mNextLevelButton = ((ImageButton) findViewById(R.id.nextLvl));
        mNextLevelButton.setVisibility(View.INVISIBLE);
        mNextLevelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAd();
            }
        });

        MobileAds.initialize(this, "ca-app-pub-8731827103414918~6135545007");

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                setGameStateTimeOut();
            }
        }, Settings.lvlDuration * 1000);

        setupStage();

        // Create the InterstitialAd and set the adUnitId (defined in values/strings.xml).
        mInterstitialAd = newInterstitialAd();
        loadInterstitial();
    }

    private InterstitialAd newInterstitialAd() {
        InterstitialAd interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.d("ad", "Loaded");
                //mNextLevelButton.setEnabled(true);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.d("ad", "failed to load");
                //mNextLevelButton.setEnabled(true);
            }

            @Override
            public void onAdClosed() {
                // Proceed to the next level.
                Log.d("ad", "ad closed");
                nextLvl();
            }
        });
        return interstitialAd;
    }

    private void loadInterstitial() {
        // Disable the next level button and load the ad.
        // mNextLevelButton.setEnabled(false);
        AdRequest adRequest = new AdRequest.Builder().setRequestAgent("android_studio:ad_template").build();
        mInterstitialAd.loadAd(adRequest);
    }



    private static boolean isCorrect(int value) {
        if (numbers.length == 1) return true;

        for (int number : numbers) {
            if (number < value) return false;
        }

        return true;
    }

    private static void updateNumbers(int value) {
        if(!Utils.contains(numbers, value)) return;

        int[] temp = new int[numbers.length - 1];

        int a = 0;
        for (int i = 0 ; i < numbers.length ; i++) {
            if (numbers[i] != value) {
                temp[a] = numbers[i];
                a++;
            }
        }

        numbers = temp;
    }

    private int[] getNumbers() {
        // TODO - needs to take into account the lvl of the player.
        Random r = new Random();

        int numberOfNumbers = 5;

        int[] list = new int[numberOfNumbers];
        for(int i = 0 ; i < numberOfNumbers; i++) {
            list[i] = r.nextInt(10);
        }

        list = Utils.distinct(list);

        return list;
    }

    private void setupStage() {

        numbers = this.getNumbers();

        ArrayList<Button> buttons = new ArrayList<>();

        for (final int number : numbers) {
            Button b = new Button(this);
            b.setBackgroundColor(Utils.getRandomBackgroundColor());
            b.setText(number + "");
            b.setId(number);

            b.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    if (state == StateEnum.timeout) return;

                    // Perform action on click
                    Button b = (Button) findViewById(number);
                    if (isCorrect(number)) {
                        b.setBackgroundColor(Color.GREEN);
                        updateNumbers(number);
                    } else {
                        b.setBackgroundColor(Color.RED);
                        setGameStateStageFailed();
                    }

                    if (isStageComplete()) setGameStateStageComplete();
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
        int width = display.getWidth();
        int height = display.getHeight();

        int buttonSize = width / 7;

        int centeringMarginWidth = width / 4;
        int centeringMarginHeight = height / 4;

        int margin = 20;

        Position[] positions = PositionHelper.getPositions(centeringMarginWidth, width - centeringMarginWidth, centeringMarginHeight, height - centeringMarginHeight, buttons.size(), buttonSize, margin);

        for (int i = 0 ; i < positions.length ; i++) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(buttonSize, buttonSize);

            layoutParams.topMargin = positions[i].y;
            layoutParams.leftMargin = positions[i].x;
            buttons.get(i).setLayoutParams(layoutParams);
        }
    }

    private boolean isStageComplete() {
        return numbers.length == 0;
    }

    private void setGameStateStageComplete() {
        state = StateEnum.stageComplete;
        Log.d("a", "complete");
        completedStages ++;

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
        state = StateEnum.timeout;
        Log.d("a", "timeout \nwins:" + completedStages + "\nlosses: " + failedStages);
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
        state = StateEnum.stageFailed;
        Log.d("a", "fail");
        failedStages ++;
        showThumbsDown();
        setupStage();
        // TODO - implement timing of each stage/lvl
    }

    public void nextLvl() {
        // prepare for the next lvl

        hideNextLvlButton();

        mInterstitialAd = newInterstitialAd();
        loadInterstitial();

        // to other stuff as well
        // TODO - initialize next lvl
    }

    private void showAd() {
        // Show the ad if it's ready. Otherwise toast and reload the ad.
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
            nextLvl();
        }
    }
}
