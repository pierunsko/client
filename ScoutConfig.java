package scout.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.lang.reflect.Field;

/**
 * Central config object. All module settings live here as public fields.
 * Persisted to .minecraft/scoutclient.json on save.
 */
public class ScoutConfig {

    // ─── Palette ────────────────────────────────────────────────────────────
    public int    paletteHue1 = 180;   // hue degrees for color 1
    public int    paletteHue2 = 270;   // hue degrees for color 2
    public float  paletteSaturation = 0.9f;
    public float  paletteBrightness  = 1.0f;
    public float  paletteSpeed       = 1.0f;

    // ─── HitBubbles ─────────────────────────────────────────────────────────
    public boolean hitBubblesEnabled    = true;
    public int     hitBubblesRenderTime = 20;   // ticks
    public int     hitBubblesScale      = 100;  // percent
    public String  hitBubblesStyle      = "CIRCLE";

    // ─── HitParticles ───────────────────────────────────────────────────────
    public boolean hitParticlesEnabled     = true;
    public int     hitParticlesCount       = 8;
    public int     hitParticlesSpeed       = 6;
    public int     hitParticlesRenderTime  = 40;  // ticks
    public float   hitParticlesScale       = 0.5f;
    public boolean hitParticlesCritOnly    = false;
    public boolean hitParticlesBounce      = true;
    public boolean hitParticlesAlphaFade   = true;
    // texture toggles
    public boolean hitParticlesFirefly   = true;
    public boolean hitParticlesStar      = false;
    public boolean hitParticlesHeart     = false;
    public boolean hitParticlesSnowflake = false;

    // ─── JumpParticles ──────────────────────────────────────────────────────
    public boolean jumpParticlesEnabled    = true;
    public int     jumpParticlesCount      = 6;
    public float   jumpParticlesScale      = 0.4f;
    public int     jumpParticlesRenderTime = 30;

    // ─── JumpCircles ────────────────────────────────────────────────────────
    public boolean jumpCirclesEnabled    = true;
    public float   jumpCirclesScale      = 1.0f;
    public int     jumpCirclesRenderTime = 20;
    public String  jumpCirclesStyle      = "CIRCLE";

    // ─── AmbientParticles ───────────────────────────────────────────────────
    public boolean ambientParticlesEnabled   = false;
    public int     ambientParticlesCount     = 20;
    public float   ambientParticlesScale     = 1.0f;
    public boolean ambientParticlesFirefly   = true;
    public boolean ambientParticlesStar      = false;
    public boolean ambientParticlesRandomColor = false;

    // ─── Trails ─────────────────────────────────────────────────────────────
    public boolean trailsEnabled     = true;
    public int     trailsLength      = 40;  // ticks
    public int     trailsHeight      = 100; // percent of entity height
    public String  trailsStyle       = "FADED";
    public boolean trailsFirstPerson = false;
    public boolean trailsHalfOnly    = false;

    // ─── TargetHUD ──────────────────────────────────────────────────────────
    public boolean targetHudEnabled    = true;
    public String  targetHudStyle      = "NORMAL";  // MINI / NORMAL / TINY
    public int     targetHudOffsetX    = 0;
    public int     targetHudOffsetY    = -60;
    public int     targetHudRenderTime = 3;  // seconds
    public boolean targetHudParticles  = true;
    public boolean targetHudFollowEntity = false;

    // ─── TargetESP ──────────────────────────────────────────────────────────
    public boolean targetEspEnabled     = true;
    public String  targetEspStyle       = "SPIRAL"; // LEGACY / SPIRAL / SCAN
    public float   targetEspScale       = 100f;
    public float   targetEspAlpha       = 80f;
    public int     targetEspLiveTime    = 3;

    // ─── CustomFog ──────────────────────────────────────────────────────────
    public boolean customFogEnabled = false;
    public float   fogStart         = 0.1f;
    public float   fogEnd           = 80f;
    public boolean fogSyncColor     = true;

    // ─── AspectRatio ────────────────────────────────────────────────────────
    public boolean aspectRatioEnabled = false;
    public float   aspectRatioFactor  = 1.0f;  // multiplier on top of normal

    // ─── Fullbright ─────────────────────────────────────────────────────────
    public boolean fullbrightEnabled = false;

    // ─── Watermark ──────────────────────────────────────────────────────────
    public boolean watermarkEnabled = true;
    public int     watermarkX       = 4;
    public int     watermarkY       = 4;

    // ─── TotemPopParticles ──────────────────────────────────────────────────
    public boolean totemPopEnabled      = true;
    public int     totemPopCount        = 30;
    public float   totemPopScale        = 0.5f;
    public int     totemPopRenderTime   = 3;  // seconds
    public boolean totemPopDefaultColor = false;
    public boolean totemPopBounce       = true;

    // ─── SwingHand ──────────────────────────────────────────────────────────
    public boolean swingHandEnabled = false;
    public float   swingHandXPos    =  0.7f;
    public float   swingHandYPos    = -0.4f;
    public float   swingHandZPos    = -0.85f;
    public float   swingHandScale   =  0.75f;
    public int     swingHandRotX    =  0;
    public int     swingHandRotY    = -13;
    public int     swingHandRotZ    =  8;
    public int     swingHandXSwing  = -55;
    public int     swingHandYSwing  =  0;
    public int     swingHandZSwing  =  90;
    public int     swingHandSpeed   =  100;

    // ─── HUD Styles ─────────────────────────────────────────────────────────
    public boolean hudHealthEnabled   = true;
    public boolean hudArmorEnabled    = true;
    public boolean hudHotbarGlow      = true;
    public boolean hudSmoothScroll    = true;
    public boolean hudPotionsEnabled  = true;
    public int     hudPotionsX        = 4;
    public int     hudPotionsY        = 60;

    // ─── HitSounds ──────────────────────────────────────────────────────────
    public boolean hitSoundsEnabled  = false;
    public float   hitSoundCrit      = 1.0f;
    public float   hitSoundStrong    = 1.0f;
    public float   hitSoundWeak      = 0.5f;
    public float   hitSoundNoDamage  = 0.3f;

    // ─── Capes ──────────────────────────────────────────────────────────────
    public boolean capesEnabled  = false;
    public String  capeSelected  = "NONE";
    public String  capeCustomUrl = "";

    // ────────────────────────────────────────────────────────────────────────

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File(
            Minecraft.getMinecraft().mcDataDir, "scoutclient.json");

    public void save() {
        try (Writer w = new FileWriter(FILE)) {
            GSON.toJson(this, w);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ScoutConfig load() {
        if (!FILE.exists()) {
            ScoutConfig cfg = new ScoutConfig();
            cfg.save();
            return cfg;
        }
        try (Reader r = new FileReader(FILE)) {
            return GSON.fromJson(r, ScoutConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new ScoutConfig();
        }
    }
}
