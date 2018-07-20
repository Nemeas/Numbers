package no.nemeas.numbers;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

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

        this.numbers = this.getNumbers();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                setGameStateTimeOut();
            }
        }, 60 * 1000);

        initializeNumbers(this.numbers);
    }

    private int[] getNumbers() {
        // TODO - implement
        // needs to take into account the lvl of the player.
        int[] list = new int[] { 1, 14, 32, 21, 89 };
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

    private void initializeNumbers(int[] numbers) {

        ArrayList<Button> buttons = new ArrayList<>();

        for (final int number : numbers) {
            Button b = new Button(this);
            b.setText(number + "");
            b.setId(number);
            b.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    if (state == State.timeout || state == State.stageFailed) return;

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

        TableLayout tableLayout = (TableLayout) findViewById(R.id.board);
        TableLayout.LayoutParams lp = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);

        for (Button button : buttons) {
            tableLayout.addView(button);
        }

    }

    private boolean isStageComplete() {
        return numbers.length == 0;
    }

    private void setGameStateStageComplete() {
        state = State.stageComplete;
        completedStages ++;
        // should show a quick thumbs up, then on to the next stage
    }

    private void setGameStateTimeOut() {
        state = State.timeout;
        // show stats
    }

    private void setGameStateStageFailed() {
        state = State.stageFailed;
        failedStages ++;
        // show a quick thumbs down, then go on to the next stage
        // TODO - implement timing of each stage/lvl
    }
}
