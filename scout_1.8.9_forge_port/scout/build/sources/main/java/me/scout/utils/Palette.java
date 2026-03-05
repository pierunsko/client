package me.scout.utils;

import me.scout.Scout;
import me.scout.config.ScoutConfig;
import java.awt.Color;
import java.util.Random;

public class Palette {
    private static final Random random = new Random();
    private static ScoutConfig cfg() { return Scout.config; }

    public static int getRawColor1() { return cfg().c1; }
    public static int getRawColor2() { return cfg().c2; }
    public static int getRawColor3() { return cfg().c3; }
    public static int getRawColor4() { return cfg().c4; }
    public static ScoutConfig.PaletteStyle getStyle() { return cfg().paletteStyle; }

    public static int getColorsCount() {
        switch (getStyle()) {
            case SOLO:    return 1;
            case DUO:     return 2;
            case TRIO:    return 3;
            case QUARTET: return 4;
            default:      return 1;
        }
    }

    public static Color getColor(float position) {
        switch (getStyle()) {
            case SOLO:    return new Color(getRawColor1());
            case DUO:     return OklabUtils.interpolate(new Color(getRawColor1()), new Color(getRawColor2()), position);
            case TRIO:    return interpolate3(position, new Color(getRawColor1()), new Color(getRawColor2()), new Color(getRawColor3()));
            case QUARTET: return interpolate4(position, new Color(getRawColor1()), new Color(getRawColor2()), new Color(getRawColor3()), new Color(getRawColor4()));
            default:      return new Color(getRawColor1());
        }
    }

    private static Color interpolate3(float t, Color c1, Color c2, Color c3) {
        if (t < 0.5f) return OklabUtils.interpolate(c1, c2, t * 2f);
        return OklabUtils.interpolate(c2, c3, (t - 0.5f) * 2f);
    }

    private static Color interpolate4(float t, Color c1, Color c2, Color c3, Color c4) {
        if (t < 1f / 3f) return OklabUtils.interpolate(c1, c2, t * 3f);
        if (t < 2f / 3f) return OklabUtils.interpolate(c2, c3, (t - 1f / 3f) * 3f);
        return OklabUtils.interpolate(c3, c4, (t - 2f / 3f) * 3f);
    }

    public static Color getBackColor() {
        int alpha = (int)(255 * (cfg().backAlpha / 100f));
        return new Color(cfg().backColor >> 16 & 0xFF, cfg().backColor >> 8 & 0xFF, cfg().backColor & 0xFF, alpha);
    }

    public static int getTextColor() { return cfg().textColor | 0xFF000000; }

    public static Color getRandomColor() {
        int steps = 20;
        float position = random.nextInt(steps) / (float)(steps - 1);
        return getColor(position);
    }
}
