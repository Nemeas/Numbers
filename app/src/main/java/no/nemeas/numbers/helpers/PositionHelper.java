package no.nemeas.numbers.helpers;

import java.util.Random;

import no.nemeas.numbers.helpers.Position;

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
        for (Position position : positions) {
            if (position != null && !isClear(position, x, y, size, margin)) return false;
        }

        return true;
    }

    private static boolean isClear(Position position, int x, int y, int size, int margin) {
        Position l1 = new Position(position.x - margin, position.y - margin);
        Position r1 = new Position(position.x + size + margin, position.y + size + margin);
        Position l2 = new Position(x, y);
        Position r2 = new Position(x + size, y + size);

        return !doOverlap(l1, r1, l2, r2);
    }

    private static boolean doOverlap(Position a1, Position a2, Position b1, Position b2) {
        return a1.x < b2.x && a2.x > b1.x && a1.y < b2.y && a2.y > b1.y;
    }

    private static int nextInt(int min, int max) {
        Random r = new Random();

        while (true) {
            int a = r.nextInt(max);
            if (a > min) return a;
        }
    }
}
