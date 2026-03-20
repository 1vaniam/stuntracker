package com.stuntracker.hud;

import com.stuntracker.ClassDetector;
import com.stuntracker.config.StunConfig;
import com.stuntracker.skill.SkillHudRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class SkillBarRenderer {

    public static final int SLOT_COUNT = 7; // Passive (display only) + Skill1-5 + Ultimate
    public static final int SLOT_W     = 26;
    public static final int SLOT_H     = 26;
    public static final int GAP        = 3;
    public static final int BAR_W      = SLOT_COUNT * (SLOT_W + GAP) - GAP;
    public static final int BAR_H      = SLOT_H + 12;

    // HUD slots: 0=Passive (display only), 1=Skill1, 2=Skill2, 3=Skill3, 4=Skill4, 5=Skill5, 6=Ultimate

    private static final int[][] COLORS = {
        { argb(200,20,30,80),  argb(255,80,120,220)  }, // Passive
        { argb(200,40,40,40),  argb(255,120,120,120) }, // Sk.1
        { argb(200,40,40,40),  argb(255,120,120,120) }, // Sk.2
        { argb(200,40,40,40),  argb(255,120,120,120) }, // Sk.3
        { argb(200,40,40,40),  argb(255,120,120,120) }, // Sk.4
        { argb(200,40,40,40),  argb(255,120,120,120) }, // Sk.5
        { argb(200,80,40,0),   argb(255,220,160,0)   }, // Ultimate
    };

    private static final Identifier PLACEHOLDER =
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/placeholder.png");

    private static final Identifier[] PLACEHOLDER_ICONS = {
        PLACEHOLDER, PLACEHOLDER, PLACEHOLDER, PLACEHOLDER,
        PLACEHOLDER, PLACEHOLDER, PLACEHOLDER,
    };

    private static final Identifier[] ASSASSIN_ICONS = {
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/assassin/deadly_calm.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/assassin/lethal_combo.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/assassin/ravaging_dash.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/assassin/death_bloom.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/assassin/shadowquake.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/assassin/crimson_arc.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/assassin/last_dance.png"),
    };

    private static final Identifier[] MAGE_ICONS = {
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/mage/passive.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/mage/skill1.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/mage/skill2.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/mage/skill3.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/mage/skill4.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/mage/skill5.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/mage/skill6.png"),
    };

    private static final Identifier[] SHAMAN_ICONS = {
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/shaman/passive.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/shaman/skill1.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/shaman/skill2.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/shaman/skill3.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/shaman/skill4.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/shaman/skill5.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/shaman/skill6.png"),
    };

    private static final Identifier[] SUMMONER_ICONS = {
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/summoner/passive.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/summoner/skill1.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/summoner/skill2.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/summoner/skill3.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/summoner/skill4.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/summoner/skill5.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/summoner/skill6.png"),
    };

    private static final Identifier[] ARCHER_ICONS = {
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/archer/passive.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/archer/skill1.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/archer/skill2.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/archer/skill3.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/archer/skill4.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/archer/skill5.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/archer/skill6.png"),
    };

    private static final Identifier[] WARRIOR_ICONS = {
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/warrior/passive.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/warrior/skill1.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/warrior/skill2.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/warrior/skill3.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/warrior/skill4.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/warrior/skill5.png"),
        Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/warrior/skill6.png"),
    };

    private SkillBarRenderer() {}

    /** Returns the class-based fallback icon for a given HUD slot index. Used by HudLayoutScreen. */
    public static net.minecraft.resources.Identifier getClassIcon(ClassDetector.PlayerClass cls, int slot) {
        Identifier[] icons =
            cls == ClassDetector.PlayerClass.AWAKENED_ASSASSIN ? ASSASSIN_ICONS :
            cls == ClassDetector.PlayerClass.AWAKENED_MAGE     ? MAGE_ICONS     :
            cls == ClassDetector.PlayerClass.AWAKENED_SHAMAN   ? SHAMAN_ICONS   :
            cls == ClassDetector.PlayerClass.AWAKENED_SUMMONER ? SUMMONER_ICONS :
            cls == ClassDetector.PlayerClass.AWAKENED_ARCHER   ? ARCHER_ICONS   :
            cls == ClassDetector.PlayerClass.AWAKENED_WARRIOR  ? WARRIOR_ICONS  :
            PLACEHOLDER_ICONS;
        if (slot < 0 || slot >= icons.length) return PLACEHOLDER;
        return icons[slot];
    }

    public static void register() {
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
            Identifier.fromNamespaceAndPath("stuntracker", "skill_bar"),
            (ctx, delta) -> render(ctx));
    }

    private static void render(GuiGraphics ctx) {
        StunConfig cfg = StunConfig.get();
        if (!cfg.enabled || !cfg.showSkillBar) return;

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        float scale = Math.max(0.5f, Math.min(2.0f, cfg.skillBarScale));
        int sw2  = (int)(SLOT_W * scale);
        int sh2  = (int)(SLOT_H * scale);
        int gap2 = (int)(GAP * scale);
        int pad2 = (int)(3 * scale);
        // Derive scaledBarW from the already-rounded perslot values to avoid rounding drift
        int scaledBarW = SLOT_COUNT * (sw2 + gap2) - gap2;
        int scaledBarH = sh2 + (int)(12 * scale);

        if (cfg.skillBarX == 0 && cfg.skillBarY == 0) {
            cfg.skillBarX = (sw - scaledBarW) / 2;
            cfg.skillBarY = sh - scaledBarH - 5;
        }

        int ox = Math.max(0, Math.min(sw - scaledBarW, cfg.skillBarX));
        int oy = Math.max(0, Math.min(sh - scaledBarH, cfg.skillBarY));

        // Background panel
        ctx.fill(ox - pad2, oy - pad2, ox + scaledBarW + pad2, oy + scaledBarH + pad2, argb(160, 0, 0, 0));
        ctx.fill(ox - pad2, oy - pad2, ox + scaledBarW + pad2, oy - pad2 + 1, argb(180, 80, 80, 80));
        ctx.fill(ox - pad2, oy + scaledBarH + pad2 - 1, ox + scaledBarW + pad2, oy + scaledBarH + pad2, argb(180, 80, 80, 80));

        long msSinceCast = System.currentTimeMillis() - SkillHudRenderer.lastCastMs;
        int lastCast = SkillHudRenderer.lastCastSlot;

        ClassDetector.PlayerClass cls = ClassDetector.getCurrentClass();
        Identifier[] classIcons =
            cls == ClassDetector.PlayerClass.AWAKENED_ASSASSIN ? ASSASSIN_ICONS :
            cls == ClassDetector.PlayerClass.AWAKENED_MAGE     ? MAGE_ICONS     :
            cls == ClassDetector.PlayerClass.AWAKENED_SHAMAN   ? SHAMAN_ICONS   :
            cls == ClassDetector.PlayerClass.AWAKENED_SUMMONER ? SUMMONER_ICONS :
            cls == ClassDetector.PlayerClass.AWAKENED_ARCHER   ? ARCHER_ICONS   :
            cls == ClassDetector.PlayerClass.AWAKENED_WARRIOR  ? WARRIOR_ICONS  :
            PLACEHOLDER_ICONS;

        boolean synced = com.stuntracker.skill.SkillSyncer.isSynced();

        for (int i = 0; i < SLOT_COUNT; i++) {
            int sx = ox + i * (sw2 + gap2);
            int sy = oy;

            boolean isActive = (lastCast == i && msSinceCast < 500);
            int bg     = isActive ? argb(230, 200, 160, 0) : COLORS[i][0];
            int border = isActive ? argb(255, 255, 210, 0)  : COLORS[i][1];

            ctx.fill(sx, sy, sx + sw2, sy + sh2, bg);
            ctx.fill(sx,         sy,       sx + sw2, sy + 1,       border);
            ctx.fill(sx,         sy + sh2-1,sx + sw2, sy + sh2,    border);
            ctx.fill(sx,         sy,       sx + 1,   sy + sh2,     border);
            ctx.fill(sx + sw2-1, sy,       sx + sw2, sy + sh2,     border);

            // Use synced icon if available, otherwise fall back to class icon
            Identifier icon = synced
                ? com.stuntracker.skill.SkillSyncer.getIconForSlot(i)
                : classIcons[i];

            ctx.blit(RenderPipelines.GUI_TEXTURED,
                icon,
                sx, sy,
                0, 0,
                sw2, sh2,
                64, 64,
                64, 64);
            if (isActive) {
                ctx.fill(sx + 1, sy + 1, sx + sw2 - 1, sy + sh2 - 1, argb(100, 255, 255, 255));
            }

            // Show keybind hint below slot to slot 0 is passive (display only, no key)
            float ts = scale * 0.65f;
            if (cls != ClassDetector.PlayerClass.NONE && i > 0) {
                int[] skillKeys = cfg.getSkillKeys();
                int keyIdx = i - 1;
                String keyLabel = "";
                if (keyIdx < skillKeys.length && skillKeys[keyIdx] != org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN) {
                    String raw = org.lwjgl.glfw.GLFW.glfwGetKeyName(skillKeys[keyIdx], 0);
                    keyLabel = (raw != null) ? raw.toUpperCase() : "";
                }
                if (!keyLabel.isEmpty()) {
                    int tw = font.width(keyLabel);
                    ctx.pose().pushMatrix();
                    ctx.pose().translate(sx + sw2 / 2f - tw * ts / 2f, sy + sh2 + (int)(2 * scale));
                    ctx.pose().scale(ts, ts);
                    ctx.drawString(font, keyLabel, 0, 0, argb(140, 200, 200, 200));
                    ctx.pose().popMatrix();
                }
            }


        }
    }

    private static int argb(int a, int r, int g, int b) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }
}
