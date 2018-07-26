package no.nemeas.numbers;

import android.util.Log;

import java.util.Random;

public class GameState {
    public StateEnum state = StateEnum.initial;
    public int completedStages = 0;
    public int failedStages = 0;
    public int lvl = 0;
    private int[] numbers;

    public void completeStage() {
        state = StateEnum.stageComplete;
        Log.d("a", "complete");
        completedStages ++;
    }

    public void failStage() {
        state = StateEnum.stageFailed;
        Log.d("a", "fail");
        failedStages ++;
    }

    public void timeOut() {
        state = StateEnum.timeout;
        Log.d("a", "timeout \nwins:" + completedStages + "\nlosses: " + failedStages);
    }

    public void newStage() {
        numbers = this.getNumbers();
    }

    public void nextLvl() {
        state = StateEnum.initial;
        completedStages = 0;
        failedStages = 0;
        ++lvl;
    }

    public int[] getStageNumbers() {
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

    public boolean isStageComplete() {
        return numbers.length == 0;
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
}
