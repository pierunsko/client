package me.scout.utils;

import java.awt.Color;

/**
 * Oklab perceptual color interpolation — ported from SoupAPI OklabUtils.java.
 */
public class OklabUtils {

    public static Color interpolate(Color c1, Color c2, float t) {
        t = Math.max(0f, Math.min(1f, t));
        float[] lab1 = rgbToOklab(c1);
        float[] lab2 = rgbToOklab(c2);
        float L = lab1[0] + (lab2[0] - lab1[0]) * t;
        float a = lab1[1] + (lab2[1] - lab1[1]) * t;
        float b = lab1[2] + (lab2[2] - lab1[2]) * t;
        int alpha = (int)(c1.getAlpha() + (c2.getAlpha() - c1.getAlpha()) * t);
        return oklabToRgb(L, a, b, alpha);
    }

    private static float[] rgbToOklab(Color c) {
        float r = srgbToLinear(c.getRed()   / 255f);
        float g = srgbToLinear(c.getGreen() / 255f);
        float b = srgbToLinear(c.getBlue()  / 255f);

        float l = 0.4122214708f * r + 0.5363325363f * g + 0.0514459929f * b;
        float m = 0.2119034982f * r + 0.6806995451f * g + 0.1073969566f * b;
        float s = 0.0883024619f * r + 0.2817188376f * g + 0.6299787005f * b;

        float l_ = cbrt(l);
        float m_ = cbrt(m);
        float s_ = cbrt(s);

        return new float[]{
            0.2104542553f * l_ + 0.7936177850f * m_ - 0.0040720468f * s_,
            1.9779984951f * l_ - 2.4285922050f * m_ + 0.4505937099f * s_,
            0.0259040371f * l_ + 0.7827717662f * m_ - 0.8086757660f * s_
        };
    }

    private static Color oklabToRgb(float L, float a, float b, int alpha) {
        float l_ = L + 0.3963377774f * a + 0.2158037573f * b;
        float m_ = L - 0.1055613458f * a - 0.0638541728f * b;
        float s_ = L - 0.0894841775f * a - 1.2914855480f * b;

        float l = l_ * l_ * l_;
        float m = m_ * m_ * m_;
        float s = s_ * s_ * s_;

        float r = linearToSrgb( 4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s);
        float g = linearToSrgb(-1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s);
        float bv = linearToSrgb(-0.0041960863f * l - 0.7034186147f * m + 1.7076147010f * s);

        int ri = Math.max(0, Math.min(255, (int)(r * 255f + 0.5f)));
        int gi = Math.max(0, Math.min(255, (int)(g * 255f + 0.5f)));
        int bi = Math.max(0, Math.min(255, (int)(bv * 255f + 0.5f)));
        int ai = Math.max(0, Math.min(255, alpha));
        return new Color(ri, gi, bi, ai);
    }

    private static float srgbToLinear(float c) {
        return c <= 0.04045f ? c / 12.92f : (float)Math.pow((c + 0.055f) / 1.055f, 2.4);
    }

    private static float linearToSrgb(float c) {
        c = Math.max(0f, Math.min(1f, c));
        return c <= 0.0031308f ? 12.92f * c : 1.055f * (float)Math.pow(c, 1f / 2.4f) - 0.055f;
    }

    private static float cbrt(float x) {
        return x >= 0 ? (float)Math.pow(x, 1.0 / 3.0) : -(float)Math.pow(-x, 1.0 / 3.0);
    }
}
