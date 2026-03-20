package com.stuntracker.shaman;

import com.stuntracker.ClassDetector;
import com.stuntracker.config.StunConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class ShamanHudRenderer {

    private ShamanHudRenderer() {}

    public static void register() {
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
            Identifier.fromNamespaceAndPath("stuntracker", "shaman_hud"),
            (ctx, delta) -> render(ctx));
    }

    private static void render(GuiGraphics ctx) {
        StunConfig cfg = StunConfig.get();
        if (!cfg.enabled || !cfg.showShamanHud) return;
        if (ClassDetector.getCurrentClass() != ClassDetector.PlayerClass.AWAKENED_SHAMAN) return;

        ShamanModeTracker.ShamanMode mode = ShamanModeTracker.getCurrentMode();
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        String label;
        int textColor, bgColor, borderColor;

        if (mode == ShamanModeTracker.ShamanMode.ATTACK) {
            label       = "⚔ Attack Mode";
            textColor   = argb(255, 255, 100, 80);
            bgColor     = argb(160, 60, 10, 10);
            borderColor = argb(200, 200, 60, 60);
        } else {
            label       = "✚ Healing Mode";
            textColor   = argb(255, 80, 255, 120);
            bgColor     = argb(160, 10, 60, 20);
            borderColor = argb(200, 60, 200, 80);
        }

        float scale = Math.max(0.5f, Math.min(2.0f, cfg.shamanHudScale));
        int padX = 6, padY = 4;
        int pillW = (int)((font.width(label) + padX * 2) * scale);
        int pillH = (int)((9 + padY * 2) * scale);

        int x = Math.max(0, Math.min(sw - pillW, cfg.shamanHudX));
        int y = Math.max(0, Math.min(sh - pillH, cfg.shamanHudY));

        ctx.fill(x, y, x + pillW, y + pillH, bgColor);
        ctx.fill(x, y, x + pillW, y + 1, borderColor);
        ctx.fill(x, y + pillH - 1, x + pillW, y + pillH, borderColor);
        ctx.fill(x, y, x + 1, y + pillH, borderColor);
        ctx.fill(x + pillW - 1, y, x + pillW, y + pillH, borderColor);

        ctx.pose().pushMatrix();
        ctx.pose().translate(x + padX * scale, y + padY * scale);
        ctx.pose().scale(scale, scale);
        ctx.drawString(font, label, 0, 0, textColor);
        ctx.pose().popMatrix();
    }

    private static int argb(int a, int r, int g, int b) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }
}
