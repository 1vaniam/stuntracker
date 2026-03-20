package com.stuntracker.hud;

import com.stuntracker.ClassDetector;
import com.stuntracker.StunState;
import com.stuntracker.config.StunConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class StunHudRenderer {

    private static final long FADE_AFTER_MS    = 3_000;
    private static final long FADE_DURATION_MS = 600;

    private StunHudRenderer() {}

    public static void register() {
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
            Identifier.fromNamespaceAndPath("stuntracker", "stun_hud"),
            (ctx, delta) -> render(ctx));
    }

    private static void render(GuiGraphics ctx) {
        StunConfig cfg = StunConfig.get();
        if (!cfg.enabled || !cfg.showHud) return;

        if (ClassDetector.getCurrentClass() != ClassDetector.PlayerClass.AWAKENED_ASSASSIN
                && ClassDetector.getCurrentClass() != ClassDetector.PlayerClass.NONE) return;

        StunState.Status status = StunState.get();
        if (status == StunState.Status.UNKNOWN) return;

        long msSince = StunState.msSinceChange();
        float alpha = msSince < FADE_AFTER_MS ? 1.0f
            : Math.max(0f, 1f - (float)(msSince - FADE_AFTER_MS) / FADE_DURATION_MS);
        if (alpha <= 0f) return;

        int a = (int)(alpha * 255);
        String label;
        int textColor, bgColor, borderColor;

        if (status == StunState.Status.READY) {
            label       = "⚡ STUN READY";
            textColor   = argb(a, 80, 255, 100);
            bgColor     = argb((int)(a * 0.55f), 0, 60, 10);
            borderColor = argb((int)(a * 0.8f), 60, 200, 60);
        } else {
            label       = "⏳ COOLDOWN";
            textColor   = argb(a, 255, 80, 80);
            bgColor     = argb((int)(a * 0.55f), 60, 0, 0);
            borderColor = argb((int)(a * 0.8f), 180, 50, 50);
        }

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        float scale = Math.max(0.5f, Math.min(2.0f, cfg.stunHudScale));
        int padX = 6, padY = 4;
        int pillW = (int)((font.width(label) + padX * 2) * scale);
        int pillH = (int)((9 + padY * 2) * scale);

        int x = Math.max(0, Math.min(sw - pillW, cfg.stunHudX));
        int y = Math.max(0, Math.min(sh - pillH, cfg.stunHudY));

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
