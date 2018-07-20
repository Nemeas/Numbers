package no.nemeas.numbers;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TableLayout;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.System.out;

enum State {
    initial,
    stageComplete,
    stageFailed,
    timeout
}

// the game should be divided into stages and lvls, where one lvl contains an infinity number of stages.
// when starting a lvl the timer begins, and the timer is always, lets say 1 minute.
// U get points based on how many stages u can do in a minute, and how many mistakes u make.
// Each successful stage results in a quick "thumbs up", followed by a new stage.
// If u press the wrong number, a quick feedback is given, and a new stage begins.
// until the timer runs out.

public class GameActivity extends AppCompatActivity {

    private static int[] numbers;
    private static State state = State.initial;
    private static int completedStages = 0;
    private static int failedStages = 0;
    private static Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);



        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                setGameStateTimeOut();
            }
        }, 60 * 1000);

        setupStage();
    }

    private int[] getNumbers() {
        // TODO - needs to take into account the lvl of the player.
        Random r = new Random();

        int numberOfNumbers = 5;

        int[] list = new int[numberOfNumbers];
        for(int i = 0 ; i < numberOfNumbers; i++) {
            list[i] = r.nextInt(10);
        }

        list = distinct(list);

        return list;
    }

    private static boolean isCorrect(int value) {
        if (numbers.length == 1) return true;

        for (int number : numbers) {
            if (number < value) return false;
        }

        return true;
    }

    private static void updateNumbers(int value) {
        if(!contains(numbers, value)) return;

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

    private void setupStage() {

        numbers = this.getNumbers();

        ArrayList<Button> buttons = new ArrayList<>();

        for (final int number : numbers) {
            Button b = new Button(this);
            b.setText(number + "");
            b.setId(number);

            b.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    if (state == State.timeout) return;

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

        relativeLayout.removeAllViews();

        for (Button button : buttons) {
            relativeLayout.addView(button);
        }
    }

    private void positionButtons(ArrayList<Button> buttons) {
        // TODO - something fishy about this positioning, also:
        // TODO - make the buttons not overlap..

        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        int buttonSize = width / 10;

        int centeringMarginWidth = width / 4;
        int centeringMarginHeight = height / 4;

        for (Button button : buttons) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(buttonSize, buttonSize);

            layoutParams.topMargin = nextInt(centeringMarginHeight, height - centeringMarginHeight - buttonSize);
            layoutParams.leftMargin = nextInt(centeringMarginWidth, width - centeringMarginWidth - buttonSize);
            button.setLayoutParams(layoutParams);
        }
    }

    private static int nextInt(int min, int max) {
        Random r = new Random();

        while (true) {
            int a = r.nextInt(max);
            if (a > min) return a;
        }
    }

    private boolean isStageComplete() {
        return numbers.length == 0;
    }

    private void setGameStateStageComplete() {
        state = State.stageComplete;
        Log.d("a", "complete");
        completedStages ++;
        // should show a quick thumbs up, then on to the next stage
        setupStage();
    }

    private void setGameStateTimeOut() {
        state = State.timeout;
        Log.d("a", "timeout");
        // show stats
    }

    private void setGameStateStageFailed() {
        state = State.stageFailed;
        Log.d("a", "fail");
        failedStages ++;
        // show a quick thumbs down, then go on to the next stage
        setupStage();
        // TODO - implement timing of each stage/lvl
    }

    private static int[] distinct(int[] list) {
        int numberOfDistinctNumbers = 0;
        ArrayList<String> checked = new ArrayList<String>();

        for (int i : list) {
            if (checked.contains(i + "")) continue;
            checked.add(i + "");
            numberOfDistinctNumbers ++;
        }

        int[] distinct = new int[numberOfDistinctNumbers];

        int a = 0;

        for (String i : checked) {
            distinct[a] = Integer.parseInt(i);
            a ++;
        }

        return distinct;
    }

    private static boolean contains(int[] list, int value) {
        for (int i : list) {
            if (i == value) return true;
        }
        return false;
    }
}
