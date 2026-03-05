package me.scout.utils;

import java.util.Random;

public class MathUtility {
    private static final Random rng = new Random();

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float random(float min, float max) {
        return min + rng.nextFloat() * (max - min);
    }
}
