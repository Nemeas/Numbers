package no.nemeas.numbers;

import android.util.Log;

public class DifficultyManager {
    public static Setup getSetup(int completedStages, int failedStages, int lvl) {
        // TODO - implement
        Log.d(GameActivity.DEBUG, "c: " + completedStages + "\nf: " + failedStages + "\nlvl: " + lvl);
        int n = (int)(5 + Math.log(lvl));
        Log.d(GameActivity.DEBUG, "n: " + n);
        return new Setup(n, 0, 10);
    }
}
