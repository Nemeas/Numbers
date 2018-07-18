package no.nemeas.numbers;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.Toast;

import java.io.Console;
import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {

    private static int[] numbers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        this.numbers = this.getNumbers();

        doStuff(this.numbers);
    }

    private int[] getNumbers() {
        int[] list = new int[] { 1, 14, 32, 21, 89 };
        return list;
    }

    private static boolean isCorrect(int value) {
        if (numbers.length == 1) return true;

        for (int number : numbers) {
            if (number < value) return false;
        }

        int[] temp = new int[numbers.length - 1];

        int a = 0;
        for (int i = 0 ; i < numbers.length ; i++) {
            if (numbers[i] != value) {
                temp[a] = numbers[i];
                a++;
            }
        }

        numbers = temp;

        return true;
    }

    private void doStuff(int[] numbers) {

        ArrayList<Button> buttons = new ArrayList<>();

        for (final int number : numbers) {
            Button b = new Button(this);
            b.setText(number + "");
            b.setId(number);
            b.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Perform action on click
                    Button b = (Button) findViewById(number);
                    if (isCorrect(number)) {
                        b.setBackgroundColor(Color.GREEN);
                    } else {
                        b.setBackgroundColor(Color.RED);
                    }
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
}
