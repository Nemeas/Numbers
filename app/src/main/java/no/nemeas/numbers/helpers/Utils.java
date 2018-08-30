package no.nemeas.numbers.helpers;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.Random;

import no.nemeas.numbers.Settings;

public class Utils {
    public static int getRandomBackgroundColor() {
        return Settings.colorPalette[new Random().nextInt(Settings.colorPalette.length - 1)];
    }

    public static int[] distinct(int[] list) {
        int numberOfDistinctNumbers = 0;
        ArrayList<String> checked = new ArrayList<String>();

        for (int i : list) {
            if (checked.contains(i + "")) continue;
            checked.add(i + "");
            numberOfDistinctNumbers ++;
        }

        int[] distinct = new int[numberOfDistinctNumbers];

        int a = 0;

        for (String i : checked) {
            distinct[a] = Integer.parseInt(i);
            a ++;
        }

        return distinct;
    }

    public static boolean contains(int[] list, int value) {
        for (int i : list) {
            if (i == value) return true;
        }
        return false;
    }

    public static String formatSecs(long secs) {
        return (secs < 10) ? "0" + secs : secs + "";
    }

    public static int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }
}
