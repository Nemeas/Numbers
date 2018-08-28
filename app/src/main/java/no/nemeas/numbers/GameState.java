package no.nemeas.numbers;

import android.util.Log;

public class GameState {
    public StateEnum state = StateEnum.initial;
    public int score = 0;
    public int lvl = 1;
    private int[] numbers;

    public void completeRound() {
        state = StateEnum.stageComplete;
        Log.d("a", "complete");
        score++;
    }

    public void failRound() {
        state = StateEnum.stageFailed;
        Log.d("a", "fail");
        if (score > 0)
            score--;
    }

    public void timeOut() {
        state = StateEnum.timeout;
    }

    public void newRound() {
        numbers = this.getNumbers();
    }

    public void nextStage() {
        state = StateEnum.initial;
        if (score > Settings.LVL_CAP) {
            lvl++;
        }
        score = 0;
    }

    public int[] getRoundNumbers() {
        return this.numbers;
    }

    public boolean isCorrect(int value) {
        if (numbers.length == 1) return true;

        for (int number : numbers) {
            if (number < value) return false;
        }

        return true;
    }

    public void removeNumber(int value) {
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

    public boolean isRoundComplete() {
        return numbers.length == 0;
    }

    private int[] getNumbers() {
        Setup setup = DifficultyManager.getSetup(score, lvl);

        int[] list = new int[setup.n];
        for(int i = 0 ; i < setup.n; i++) {
            list[i] = Utils.getRandomNumberInRange(setup.min, setup.max);
        }

        list = Utils.distinct(list);

        return list;
    }
}
