package no.nemeas.numbers;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
// needs a countdown before a lvl starts - done

// needs to increase difficulty based on lvl.

public class GamePlayFragment extends Fragment implements Timer.Listener, Ad.Listener {

    private Ad ad;
    private GameState state = new GameState();
    private Timer timer = new Timer().setDuration(Settings.DURATION_OF_LVL_IN_MILLI_SECS).setListener(this);
    private int width;
    private int height;

    // State
    private boolean stageComplete = false;
    private boolean doNothing = false;

    // Views
    private TextView mTextTimer;
    private TextView mScore;
    private TextView mReadyText;
    private TextView mSetText;
    private TextView mGoText;
    private RelativeLayout mBoard;
    private ImageView mThumbsUp;
    private ImageView mThumbsDown;

    private View mView;

    // Listeners
    private Listener mListener;

    @Override
    public void OnAdClosed() {
        nextStage();
    }

    @Override
    public void OnAdFailToLoad() {
        nextStage();
    }

    interface Listener {
        void onNewHighScore(int score);
        void onBack();
    }

    public GamePlayFragment setListener(Listener listener) {
        mListener = listener;
        return this;
    }

    public GamePlayFragment setScreenSize(Point point) {
        this.height = point.y;
        this.width = point.x;
        return this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.activity_game, container, false);

        // cache views
        mTextTimer = mView.findViewById(R.id.textTimer);
        mBoard = mView.findViewById(R.id.board);
        mReadyText = mView.findViewById(R.id.readyText);
        mSetText = mView.findViewById(R.id.setText);
        mGoText = mView.findViewById(R.id.goText);
        mThumbsDown = mView.findViewById(R.id.thumbsDown);
        mThumbsUp = mView.findViewById(R.id.thumbsUp);
        mScore = mView.findViewById(R.id.score);

        ad = new Ad(mView.getContext()).setListener(this);
        nextStage();

        return mView;
    }

    @Override
    public void onPause() {
        super.onPause();
        this.timer.pause();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(this.stageComplete)
            return;

        if(this.timer.paused)
            this.timer.resume();
    }

    private void setupNewRound() {
        state.newRound();

        ArrayList<Button> buttons = new ArrayList<>();

        for (final int number : state.getRoundNumbers()) {
            Button b = new Button(mView.getContext());
            b.setBackgroundColor(Utils.getRandomBackgroundColor());
            b.setText(number + "");
            b.setId(number);

            b.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                if (state.state == StateEnum.timeout) return;

                // Perform action on click
                Button b = mView.findViewById(number);
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

        mBoard.removeAllViewsInLayout();

        for (Button button : buttons) {
            mBoard.addView(button);
        }
    }

    private void positionButtons(ArrayList<Button> buttons) {
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

        updateScore();

        showThumbsUp();

        setupNewRound();
    }

    private void updateScore() {
        mScore.setText((state.score) + "");
    }

    private void showThumbsUp() {
        animateImage(mThumbsUp, Settings.SUCCESS_IMAGE_DURATION);
    }

    private void showThumbsDown() {
        animateImage(mThumbsDown, Settings.FAILURE_IMAGE_DURATION);
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
        state.timeOut();
        timer.stop();
        this.stageComplete = true;
        mScore.setText("");
        this.mListener.onNewHighScore(state.score);
        showNextStageDialog();
    }

    private void showNextStageDialog() {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(mView.getContext());

        // 2. Chain together various setter methods to set the dialog characteristics
        String title = getCompleteTitle(state.score);
        builder.setMessage("Score: " + (state.score)).setTitle(title);

        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ad.showAd();
            }
        });
        builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mListener.onBack();
            }
        });

        builder.setCancelable(false);

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        dialog.show();
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

    private void setGameStateRoundFailed() {
        state.failRound();
        updateScore();
        showThumbsDown();
        setupNewRound();
    }

    public void nextStage() {

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
                if (!doNothing) {
                    showBoard();
                    updateScore();
                    showTimer();
                    timer.start();
                }
            }
        }, Settings.DURATION_OF_COUNTDOWN_IN_MILLI_SECS);
    }

    private void hideTimer() {
        mTextTimer.setVisibility(View.INVISIBLE);
    }

    private void showTimer() {
        mTextTimer.setVisibility(View.VISIBLE);
    }

    private void hideBoard() {
        mBoard.setVisibility(View.INVISIBLE);
    }

    private void showCountDown() {
        int duration = Settings.DURATION_OF_COUNTDOWN_IN_MILLI_SECS / 3;

        animateTextView(mReadyText, 0, duration);
        animateTextView(mSetText, duration, duration);
        animateTextView(mGoText, duration * 2, duration);
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
        mBoard.setVisibility(View.VISIBLE);
    }

    @Override
    public void onTimerUpdate(long millisRemaining) {
        long secs = millisRemaining / 1000;
        mTextTimer.setText("" + (secs / 60) + ":" + Utils.formatSecs(secs % 60));
    }

    @Override
    public void onTimerFinished() {
        mTextTimer.setText("0:00");
        setGameStateTimeOut();
    }
}
