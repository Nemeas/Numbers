package no.nemeas.numbers;

import java.util.Random;

public class PositionHelper {
    public static Position[] getPositions(int xMin, int xMax, int yMin, int yMax, int n, int size, int margin) {

        Position[] res = new Position[n];

        for(int i = 0 ; i < n ; i++) {
            res[i] = getPosition(res, xMin, xMax, yMin, yMax, size, margin);
        }

        return res;

    }

    private static Position getPosition(Position[] current, int xMin, int xMax, int yMin, int yMax, int size, int margin) {
        int x = nextInt(xMin, xMax);
        int y = nextInt(yMin, yMax);

        if (isClear(current, x, y, size, margin)) return new Position(x, y);
        return getPosition(current, xMin, xMax, yMin, yMax, size, margin);
    }

    private static boolean isClear(Position[] positions, int x, int y, int size, int margin) {
        // TODO - implement
        return true;
    }

    private static int nextInt(int min, int max) {
        Random r = new Random();

        while (true) {
            int a = r.nextInt(max);
            if (a > min) return a;
        }
    }
}
