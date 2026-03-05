package me.scout.config;

import me.scout.modules.TargetHud;

import java.awt.Color;

/**
 * Central config for Scout — mirrors SoupAPI_Config fields used by TargetHud/Renderer.
 * In 1.8.9 Forge we keep this as a plain POJO; a GUI/JSON persistence layer
 * can be wired up on top without changing any module logic.
 */
public class ScoutConfig {

    // ── TargetHud ──────────────────────────────────────────────────────────────
    public boolean targetHudEnabled       = true;
    public TargetHud.Style targetHudStyle = TargetHud.Style.NORMAL;
    public float   targetHudRenderTime    = 3.0f;   // seconds to keep HUD visible after losing target
    public boolean targetHudFollow        = false;  // follow entity in world-space
    public int     targetHudOffsetX       = 0;
    public int     targetHudOffsetY       = 0;
    public float   targetHudEntityOffsetX = 0f;
    public float   targetHudEntityOffsetY = 0f;

    // ── Palette ────────────────────────────────────────────────────────────────
    /** Raw ARGB palette colors (match SoupAPI c1-c4). */
    public int c1 = 0xFF7B2FBE;   // purple
    public int c2 = 0xFF4A90E2;   // blue
    public int c3 = 0xFF50E3C2;   // teal
    public int c4 = 0xFFE25C5C;   // red

    public PaletteStyle paletteStyle = PaletteStyle.DUO;

    // ── Appearance ─────────────────────────────────────────────────────────────
    public int   backColor        = 0x1A1A1A;  // RGB only – alpha applied separately
    public float backAlpha        = 85f;       // 0-100 %
    public int   textColor        = 0xFFFFFF;  // RGB (alpha forced to FF)
    public boolean blurShadowEnabled = true;

    // ── ModuleSupressor stubs ──────────────────────────────────────────────────
    public boolean hideHPBar       = false;
    public boolean hideItemOverlay = false;

    public enum PaletteStyle { SOLO, DUO, TRIO, QUARTET }
}
