package me.scout.config;

import me.scout.modules.JumpCircles;
import me.scout.modules.TargetHud;
import me.scout.modules.Trails;

public class ScoutConfig {
    // Palette
    public int c1 = 0xFF7B2FBE;
    public int c2 = 0xFF4A90E2;
    public int c3 = 0xFF50E3C2;
    public int c4 = 0xFFE25C5C;
    public PaletteStyle paletteStyle = PaletteStyle.DUO;

    // Appearance
    public int   backColor        = 0x1A1A1A;
    public float backAlpha        = 85f;
    public int   textColor        = 0xFFFFFF;
    public boolean blurShadowEnabled = true;

    // TargetHud
    public boolean         targetHudEnabled    = true;
    public TargetHud.Style targetHudStyle      = TargetHud.Style.NORMAL;
    public float           targetHudRenderTime = 3.0f;
    public boolean         targetHudFollow     = false;
    public int             targetHudOffsetX    = 0;
    public int             targetHudOffsetY    = 0;
    public boolean         hideHPBar           = false;
    public boolean         hideItemOverlay     = false;

    // Trails
    public boolean      trailsEnabled     = false;
    public int          trailsLength      = 40;
    public float        trailsHeight      = 100f;
    public boolean      trailsRenderHalf  = false;
    public boolean      trailsFirstPerson = false;
    public boolean      trailsForGliders  = false;
    public float        trailsAlphaFactor = 50f;
    public Trails.Style trailsStyle       = Trails.Style.FADED;

    // JumpCircles
    public boolean           jumpCirclesEnabled  = false;
    public float             jumpCirclesScale    = 100f;
    public float             jumpCirclesLiveTime = 1.5f;
    public float             jumpCirclesSpinSpeed = 180f;
    public int               jumpCirclesAlpha    = 80;
    public boolean           jumpCirclesFadeOut  = true;
    public JumpCircles.Style jumpCirclesStyle    = JumpCircles.Style.CIRCLE;

    public enum PaletteStyle { SOLO, DUO, TRIO, QUARTET }
}
