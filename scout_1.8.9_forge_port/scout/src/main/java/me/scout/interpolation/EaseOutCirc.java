package me.scout.interpolation;

/**
 * Ease-out circular animation helper — ported from SoupAPI EaseOutCirc.java.
 */
public class EaseOutCirc {

    private float progress = 0f;
    private float target   = 0f;
    private static final float SPEED = 0.15f;

    public void setTarget(float t) { this.target = t; }
    public float getProgress()     { return progress; }

    public void update(float normalizedDelta) {
        progress += (target - progress) * SPEED * normalizedDelta;
    }

    public static float ease(float t) {
        t = Math.max(0f, Math.min(1f, t));
        return (float)Math.sqrt(1.0 - Math.pow(t - 1.0, 2.0));
    }
}
