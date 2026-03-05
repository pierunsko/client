package me.scout.config;

import me.scout.modules.TargetHud;
import me.scout.modules.Trails;
import me.scout.modules.JumpCircles;

import java.awt.Color;

public class ScoutConfig {

    // ── Palette ────────────────────────────────────────────────────────────────
    public int c1 = 0xFF7B2FBE;
    public int c2 = 0xFF4A90E2;
    public int c3 = 0xFF50E3C2;
    public int c4 = 0xFFE25C5C;
    public PaletteStyle paletteStyle = PaletteStyle.DUO;

    // ── Appearance ─────────────────────────────────────────────────────────────
    public int   backColor        = 0x1A1A1A;
    public float backAlpha        = 85f;
    public int   textColor        = 0xFFFFFF;
    public boolean blurShadowEnabled = true;

    // ── TargetHud ──────────────────────────────────────────────────────────────
    public boolean        targetHudEnabled       = true;
    public TargetHud.Style targetHudStyle        = TargetHud.Style.NORMAL;
    public float          targetHudRenderTime    = 3.0f;
    public boolean        targetHudFollow        = false;
    public int            targetHudOffsetX       = 0;
    public int            targetHudOffsetY       = 0;
    public float          targetHudEntityOffsetX = 0f;
    public float          targetHudEntityOffsetY = 0f;
    public boolean        hideHPBar              = false;
    public boolean        hideItemOverlay        = false;

    // ── Trails ─────────────────────────────────────────────────────────────────
    public boolean      trailsEnabled     = false;
    public int          trailsLength      = 40;
    public float        trailsHeight      = 100f;   // percent of entity height
    public boolean      trailsRenderHalf  = false;
    public boolean      trailsFirstPerson = false;
    public boolean      trailsForGliders  = false;
    public float        trailsAlphaFactor = 50f;
    public Trails.Style trailsStyle       = Trails.Style.FADED;

    // ── JumpCircles ────────────────────────────────────────────────────────────
    public boolean              jumpCirclesEnabled   = false;
    public float                jumpCirclesScale     = 100f;
    public float                jumpCirclesLiveTime  = 1.5f;
    public float                jumpCirclesSpinSpeed = 180f;  // degrees/sec
    public int                  jumpCirclesAlpha     = 80;    // 0-100
    public boolean              jumpCirclesFadeOut   = true;
    public JumpCircles.Style    jumpCirclesStyle     = JumpCircles.Style.CIRCLE;

    // ── HitParticles ──────────────────────────────────────────────────────────
    public boolean hitParticlesEnabled = false;

    // ── AmbientParticles ──────────────────────────────────────────────────────
    public boolean ambientParticlesEnabled = false;

    // ── Halo ──────────────────────────────────────────────────────────────────
    public boolean haloEnabled = false;
    public float   haloRadius  = 1.5f;
    public float   haloAlpha   = 60f;

    public enum PaletteStyle { SOLO, DUO, TRIO, QUARTET }
}
