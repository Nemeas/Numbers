package no.nemeas.numbers;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;

import java.util.ArrayList;

enum State {
    initial,
    win,
    gameOver,
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        this.numbers = this.getNumbers();

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

                    if (state == State.gameOver) return;

                    // Perform action on click
                    Button b = (Button) findViewById(number);
                    if (isCorrect(number)) {
                        b.setBackgroundColor(Color.GREEN);
                        updateNumbers(number);
                    } else {
                        b.setBackgroundColor(Color.RED);
                        setGameStateGameOver();
                    }

                    if (isGameWon()) setGameStateGameWon();
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

    private boolean isGameWon() {
        return numbers.length == 0;
    }

    private void setGameStateGameWon() {
        state = State.win;
        // display some kind of good-job, message, with some data of how this stage went?
        // lvl up
        // dismissing the message should trigger next stage?
    }

    private void setGameStateGameOver() {
        state = State.gameOver;
        // spawn some "good job, try again for a even better time"-activity
        // TODO - implement timing of each stage/lvl
        // TODO - for future implementation; this might be a good spot to place some kind of ads.. like, every 3rd time u fail, u get an ad?
    }
}
