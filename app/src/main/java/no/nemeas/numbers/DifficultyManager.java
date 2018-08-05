package no.nemeas.numbers;

import android.util.Log;

public class DifficultyManager {
    public static Setup getSetup(int completedStages, int failedStages, int lvl) {
        // TODO - implement
        Log.d("dif", "c: " + completedStages + "\nf: " + failedStages + "\nlvl: " + lvl);
        int n = (int)(5 + Math.log(lvl));
        Log.d("dif", "n: " + n);
        return new Setup(n, 0, 10);
    }
}
