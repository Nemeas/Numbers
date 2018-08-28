package no.nemeas.numbers;

public class DifficultyManager {
    public static Setup getSetup(int score, int lvl) {
        int n = (int)(5 + Math.log(lvl));
        return new Setup(n, 0, 10);
    }
}
