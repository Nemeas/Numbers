package no.nemeas.numbers;

public class DifficultyManager {
    public static Setup getSetup(int completedStages, int failedStages, int lvl) {
        int n = (int)(5 + Math.log(lvl));
        return new Setup(n, 0, 10);
    }
}
