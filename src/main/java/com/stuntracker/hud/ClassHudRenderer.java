package com.stuntracker.hud;

import com.stuntracker.ClassDetector;
import com.stuntracker.config.StunConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class ClassHudRenderer {

    private static final int ICON = 16;
    private static final int PAD_X = 4, PAD_Y = 3;

    private ClassHudRenderer() {}

    public static void register() {
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
            Identifier.fromNamespaceAndPath("stuntracker", "class_hud"),
            (ctx, delta) -> render(ctx));
    }

    private static void render(GuiGraphics ctx) {
        StunConfig cfg = StunConfig.get();
        if (!cfg.enabled || !cfg.showClassHud) return;

        ClassDetector.PlayerClass cls = ClassDetector.getCurrentClass();
        if (cls == ClassDetector.PlayerClass.NONE) return;

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        float scale = Math.max(0.5f, Math.min(2.0f, cfg.classHudScale));
        String label = cls.displayName();

        int rawW = ICON + PAD_X + font.width(label) + PAD_X * 2;
        int rawH = Math.max(ICON, 9) + PAD_Y * 2;
        int pillW = (int)(rawW * scale);
        int pillH = (int)(rawH * scale);

        int x = Math.max(0, Math.min(sw - pillW, cfg.classHudX));
        int y = Math.max(0, Math.min(sh - pillH, cfg.classHudY));

        // Background + border
        ctx.fill(x, y, x + pillW, y + pillH, argb(160, 0, 0, 0));
        ctx.fill(x, y, x + pillW, y + 1, argb(200, 100, 180, 255));
        ctx.fill(x, y + pillH - 1, x + pillW, y + pillH, argb(200, 100, 180, 255));
        ctx.fill(x, y, x + 1, y + pillH, argb(200, 100, 180, 255));
        ctx.fill(x + pillW - 1, y, x + pillW, y + pillH, argb(200, 100, 180, 255));

        // Scaled color icon
        int[] col = classColors(cls);
        int ix = x + (int)(PAD_X * scale);
        int iy = y + (int)(PAD_Y * scale);
        int is = (int)(ICON * scale);
        ctx.fill(ix, iy, ix + is, iy + is, col[0]);
        ctx.fill(ix, iy, ix + is, iy + 1, col[1]);
        ctx.fill(ix, iy + is - 1, ix + is, iy + is, col[1]);
        ctx.fill(ix, iy, ix + 1, iy + is, col[1]);
        ctx.fill(ix + is - 1, iy, ix + is, iy + is, col[1]);

        // Initial letter centered in icon box
        String init = label.substring(0, 1);
        ctx.pose().pushMatrix();
        ctx.pose().translate(ix + is / 2f - font.width(init) * scale / 2f, iy + is / 2f - 4.5f * scale);
        ctx.pose().scale(scale, scale);
        ctx.drawString(font, init, 0, 0, col[1]);
        ctx.pose().popMatrix();

        // Class name to the right of icon
        ctx.pose().pushMatrix();
        ctx.pose().translate(x + (PAD_X + ICON + PAD_X) * scale, y + PAD_Y * scale + (ICON * scale - 9 * scale) / 2f);
        ctx.pose().scale(scale, scale);
        ctx.drawString(font, label, 0, 0, argb(255, 150, 210, 255));
        ctx.pose().popMatrix();
    }

    private static int[] classColors(ClassDetector.PlayerClass cls) {
        return switch (cls) {
            case AWAKENED_ASSASSIN -> new int[]{ argb(200, 40, 10, 60),  argb(255, 180, 80,  220) };
            case AWAKENED_ARCHER   -> new int[]{ argb(200, 10, 40, 20),  argb(255, 80,  200, 80)  };
            case AWAKENED_MAGE     -> new int[]{ argb(200, 10, 20, 60),  argb(255, 80,  120, 255) };
            case AWAKENED_WARRIOR  -> new int[]{ argb(200, 60, 20, 10),  argb(255, 220, 100, 50)  };
            case AWAKENED_SHAMAN   -> new int[]{ argb(200, 40, 50, 10),  argb(255, 150, 220, 50)  };
            case AWAKENED_SUMMONER -> new int[]{ argb(200, 50, 10, 40),  argb(255, 200, 80,  180) };
            default                -> new int[]{ argb(200, 40, 40, 40),  argb(255, 160, 160, 160) };
        };
    }

    private static int argb(int a, int r, int g, int b) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }
}
