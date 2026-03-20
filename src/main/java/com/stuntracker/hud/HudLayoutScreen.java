package com.stuntracker.hud;

import com.stuntracker.ClassDetector;
import com.stuntracker.config.StunConfig;
import com.stuntracker.hud.SkillBarRenderer;
import com.stuntracker.skill.SkillHudRenderer;
import com.stuntracker.skill.SkillSyncer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * Single unified layout screen to drag them mandem all HUD elements at once.
 * Replaces the four separate edit screens.
 */
public class HudLayoutScreen extends Screen {

    private final Screen parent;
    private static final int SNAP = 16;

    // Which widget is being dragged (-1 = none)
    // 0 = Skill Bar, 1 = Stun HUD, 2 = Class HUD, 3 = Shaman HUD
    private int dragging = -1;
    private int dragOffX, dragOffY;

    public HudLayoutScreen(Screen parent) {
        super(Component.literal("Edit HUD Layout"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Done"), btn -> onClose())
            .bounds(width / 2 - 110, height - 28, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Reset All"), btn -> resetAll())
            .bounds(width / 2 + 10, height - 28, 100, 20).build());
    }

    private void resetAll() {
        StunConfig cfg = StunConfig.get();
        Minecraft mc = Minecraft.getInstance();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        float scale = Math.max(0.5f, Math.min(2.0f, cfg.skillBarScale));
        int bw = (int)(SkillBarRenderer.BAR_W * scale);
        int bh = (int)(SkillBarRenderer.BAR_H * scale);
        cfg.skillBarX = (sw - bw) / 2;
        cfg.skillBarY = sh - bh - 5;
        cfg.stunHudX  = sw / 2 - 40;
        cfg.stunHudY  = 4;
        cfg.classHudX = 4;
        cfg.classHudY = 20;
        cfg.shamanHudX = 4;
        cfg.shamanHudY = 36;
        save();
    }

    // Drag handling

    @Override
    public void tick() {
        super.tick();
        long window = GLFW.glfwGetCurrentContext();
        if (window == 0L) return;

        double[] mx = new double[1], my = new double[1];
        GLFW.glfwGetCursorPos(window, mx, my);
        Minecraft mc = Minecraft.getInstance();
        double scale = mc.getWindow().getGuiScale();
        int mouseX = (int)(mx[0] / scale);
        int mouseY = (int)(my[0] / scale);
        boolean down = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        StunConfig cfg = StunConfig.get();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        if (!down) { dragging = -1; return; }

        if (dragging == -1) {
            // Try to start drag check all widgets in priority order
            // 0: Skill Bar
            SkillBarBounds sb = skillBarBounds(cfg, sw, sh);
            if (hit(mouseX, mouseY, sb.x - 3, sb.y - 3, sb.x + sb.w + 3, sb.y + sb.h + 3)) {
                dragging = 0; dragOffX = mouseX - sb.x; dragOffY = mouseY - sb.y; return;
            }
            // 1: Stun HUD
            StunBounds stun = stunBounds(cfg);
            if (hit(mouseX, mouseY, stun.x, stun.y, stun.x + stun.w, stun.y + stun.h)) {
                dragging = 1; dragOffX = mouseX - stun.x; dragOffY = mouseY - stun.y; return;
            }
            // 2: Class HUD
            ClassBounds cls = classBounds(cfg, mc);
            if (hit(mouseX, mouseY, cls.x, cls.y, cls.x + cls.w, cls.y + cls.h)) {
                dragging = 2; dragOffX = mouseX - cls.x; dragOffY = mouseY - cls.y; return;
            }
            // 3: Shaman HUD
            ShamanBounds sh2 = shamanBounds(cfg, mc);
            if (hit(mouseX, mouseY, sh2.x, sh2.y, sh2.x + sh2.w, sh2.y + sh2.h)) {
                dragging = 3; dragOffX = mouseX - sh2.x; dragOffY = mouseY - sh2.y; return;
            }
        } else {
            // Apply drag
            switch (dragging) {
                case 0 -> {
                    SkillBarBounds sb = skillBarBounds(cfg, sw, sh);
                    int nx = clamp(mouseX - dragOffX, 0, sw - sb.w);
                    int ny = clamp(mouseY - dragOffY, 0, sh - sb.h);
                    if (Math.abs(nx - (sw - sb.w) / 2) <= SNAP) nx = (sw - sb.w) / 2;
                    if (Math.abs(ny - (sh - sb.h) / 2) <= SNAP) ny = (sh - sb.h) / 2;
                    cfg.skillBarX = nx; cfg.skillBarY = ny;
                }
                case 1 -> {
                    StunBounds b = stunBounds(cfg);
                    int nx = clamp(mouseX - dragOffX, 0, sw - b.w);
                    int ny = clamp(mouseY - dragOffY, 0, sh - b.h);
                    if (Math.abs(nx - (sw - b.w) / 2) <= SNAP) nx = (sw - b.w) / 2;
                    if (Math.abs(ny - (sh - b.h) / 2) <= SNAP) ny = (sh - b.h) / 2;
                    cfg.stunHudX = nx; cfg.stunHudY = ny;
                }
                case 2 -> {
                    ClassBounds b = classBounds(cfg, mc);
                    int nx = clamp(mouseX - dragOffX, 0, sw - b.w);
                    int ny = clamp(mouseY - dragOffY, 0, sh - b.h);
                    if (Math.abs(nx - (sw - b.w) / 2) <= SNAP) nx = (sw - b.w) / 2;
                    if (Math.abs(ny - (sh - b.h) / 2) <= SNAP) ny = (sh - b.h) / 2;
                    cfg.classHudX = nx; cfg.classHudY = ny;
                }
                case 3 -> {
                    ShamanBounds b = shamanBounds(cfg, mc);
                    int nx = clamp(mouseX - dragOffX, 0, sw - b.w);
                    int ny = clamp(mouseY - dragOffY, 0, sh - b.h);
                    if (Math.abs(nx - (sw - b.w) / 2) <= SNAP) nx = (sw - b.w) / 2;
                    if (Math.abs(ny - (sh - b.h) / 2) <= SNAP) ny = (sh - b.h) / 2;
                    cfg.shamanHudX = nx; cfg.shamanHudY = ny;
                }
            }
            save();
        }
    }

    // Render Handler

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        StunConfig cfg = StunConfig.get();
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        // Dim background
        ctx.fill(0, 0, sw, sh, argb(120, 0, 0, 0));

        // Snap guide lines
        ctx.fill(sw / 2, 0, sw / 2 + 1, sh, argb(40, 255, 255, 0));
        ctx.fill(0, sh / 2, sw, sh / 2 + 1, argb(40, 255, 255, 0));

        // Draw each HUD widget
        renderSkillBar(ctx, cfg, mc, font, sw, sh, mouseX, mouseY);
        renderStunHud(ctx, cfg, font, sw, sh, mouseX, mouseY);
        renderClassHud(ctx, cfg, mc, font, sw, sh, mouseX, mouseY);
        renderShamanHud(ctx, cfg, mc, font, sw, sh, mouseX, mouseY);

        // Instructions
        ctx.drawCenteredString(font, "§eDrag any element to reposition  §7(snaps to center)", sw / 2, 10, 0xFFFFFF);

        super.render(ctx, mouseX, mouseY, delta);
    }

    // Skill Bar 

    private void renderSkillBar(GuiGraphics ctx, StunConfig cfg, Minecraft mc, Font font,
                                int sw, int sh, int mouseX, int mouseY) {
        if (!cfg.showSkillBar) return;
        SkillBarBounds b = skillBarBounds(cfg, sw, sh);
        boolean active = dragging == 0;
        boolean hovered = !active && hit(mouseX, mouseY, b.x - 3, b.y - 3, b.x + b.w + 3, b.y + b.h + 3);

        // Snap guide
        if (active || hovered) {
            if (Math.abs(b.x - (sw - b.w) / 2) <= SNAP)
                ctx.fill(sw / 2, 0, sw / 2 + 1, sh, argb(120, 255, 255, 0));
        }

        // Background panel
        ctx.fill(b.x - 3, b.y - 3, b.x + b.w + 3, b.y + b.h + 3, argb(active ? 200 : 160, 0, 0, 0));
        ctx.fill(b.x - 3, b.y - 3, b.x + b.w + 3, b.y - 2,
            argb(active ? 255 : 180, active ? 255 : 80, active ? 210 : 80, 0));

        // Draw actual skill slots using the real renderer data
        float scale = Math.max(0.5f, Math.min(2.0f, cfg.skillBarScale));
        int sw2  = (int)(SkillBarRenderer.SLOT_W * scale);
        int sh2  = (int)(SkillBarRenderer.SLOT_H * scale);
        int gap2 = (int)(SkillBarRenderer.GAP * scale);

        ClassDetector.PlayerClass cls = ClassDetector.getCurrentClass();
        boolean synced = SkillSyncer.isSynced();
        long msSinceCast = System.currentTimeMillis() - SkillHudRenderer.lastCastMs;
        int lastCast = SkillHudRenderer.lastCastSlot;

        int[][] COLORS = {
            { argb(200,20,30,80),  argb(255,80,120,220)  },
            { argb(200,40,40,40),  argb(255,120,120,120) },
            { argb(200,40,40,40),  argb(255,120,120,120) },
            { argb(200,40,40,40),  argb(255,120,120,120) },
            { argb(200,40,40,40),  argb(255,120,120,120) },
            { argb(200,40,40,40),  argb(255,120,120,120) },
            { argb(200,80,40,0),   argb(255,220,160,0)   },
        };

        for (int i = 0; i < SkillBarRenderer.SLOT_COUNT; i++) {
            int sx = b.x + i * (sw2 + gap2);
            int sy = b.y;

            boolean isActiveCast = (lastCast == i && msSinceCast < 500);
            int bg     = isActiveCast ? argb(230, 200, 160, 0) : COLORS[i][0];
            int border = isActiveCast ? argb(255, 255, 210, 0)  : COLORS[i][1];

            ctx.fill(sx, sy, sx + sw2, sy + sh2, bg);
            ctx.fill(sx,         sy,       sx + sw2, sy + 1,       border);
            ctx.fill(sx,         sy + sh2-1,sx + sw2, sy + sh2,    border);
            ctx.fill(sx,         sy,       sx + 1,   sy + sh2,     border);
            ctx.fill(sx + sw2-1, sy,       sx + sw2, sy + sh2,     border);

            // Use synced icon if available, otherwise fall back to class icon
            Identifier icon = synced
                ? SkillSyncer.getIconForSlot(i)
                : SkillBarRenderer.getClassIcon(cls, i);

            ctx.blit(RenderPipelines.GUI_TEXTURED,
                icon, sx, sy, 0, 0, sw2, sh2, 64, 64, 64, 64);

            if (isActiveCast)
                ctx.fill(sx + 1, sy + 1, sx + sw2 - 1, sy + sh2 - 1, argb(100, 255, 255, 255));
        }

        // Label
        String label = active ? "§e[Skill Bar]" : hovered ? "§7[Skill Bar — drag]" : "";
        if (!label.isEmpty())
            ctx.drawCenteredString(font, label, b.x + b.w / 2, b.y - 14, 0xFFFFFF);
    }

    // Stun HUD 

    private void renderStunHud(GuiGraphics ctx, StunConfig cfg, Font font,
                               int sw, int sh, int mouseX, int mouseY) {
        if (!cfg.showHud) return;
        StunBounds b = stunBounds(cfg);
        boolean active = dragging == 1;
        boolean hovered = !active && hit(mouseX, mouseY, b.x, b.y, b.x + b.w, b.y + b.h);
        int rim = active ? argb(255, 255, 210, 0) : argb(200, 60, 200, 60);

        ctx.fill(b.x, b.y, b.x + b.w, b.y + b.h, argb(active || hovered ? 220 : 160, 0, 60, 10));
        ctx.fill(b.x, b.y, b.x + b.w, b.y + 1, rim);
        ctx.fill(b.x, b.y + b.h - 1, b.x + b.w, b.y + b.h, rim);
        ctx.fill(b.x, b.y, b.x + 1, b.y + b.h, rim);
        ctx.fill(b.x + b.w - 1, b.y, b.x + b.w, b.y + b.h, rim);
        ctx.drawString(font, "⚡ STUN READY", b.x + 6, b.y + 4, argb(255, 80, 255, 100));

        if (active || hovered)
            ctx.drawCenteredString(font, active ? "§e[Stun HUD]" : "§7[Stun HUD — drag]",
                b.x + b.w / 2, b.y - 11, 0xFFFFFF);
    }

    // Class HUD 

    private void renderClassHud(GuiGraphics ctx, StunConfig cfg, Minecraft mc, Font font,
                                int sw, int sh, int mouseX, int mouseY) {
        if (!cfg.showClassHud) return;
        ClassBounds b = classBounds(cfg, mc);
        boolean active = dragging == 2;
        boolean hovered = !active && hit(mouseX, mouseY, b.x, b.y, b.x + b.w, b.y + b.h);

        ClassDetector.PlayerClass cls = ClassDetector.getCurrentClass();
        String label = cls != ClassDetector.PlayerClass.NONE ? cls.displayName() : "Awakened Assassin";
        int border = argb(200, 100, 180, 255);

        ctx.fill(b.x, b.y, b.x + b.w, b.y + b.h, argb(active || hovered ? 200 : 160, 0, 0, 0));
        ctx.fill(b.x, b.y, b.x + b.w, b.y + 1, border);
        ctx.fill(b.x, b.y + b.h - 1, b.x + b.w, b.y + b.h, border);
        ctx.fill(b.x, b.y, b.x + 1, b.y + b.h, border);
        ctx.fill(b.x + b.w - 1, b.y, b.x + b.w, b.y + b.h, border);
        ctx.drawString(font, "✦ " + label, b.x + 4, b.y + 5, argb(255, 180, 210, 255));

        if (active || hovered)
            ctx.drawCenteredString(font, active ? "§e[Class HUD]" : "§7[Class HUD — drag]",
                b.x + b.w / 2, b.y - 11, 0xFFFFFF);
    }

    // Shaman HUD 

    private void renderShamanHud(GuiGraphics ctx, StunConfig cfg, Minecraft mc, Font font,
                                 int sw, int sh, int mouseX, int mouseY) {
        if (!cfg.showShamanHud) return;
        ShamanBounds b = shamanBounds(cfg, mc);
        boolean active = dragging == 3;
        boolean hovered = !active && hit(mouseX, mouseY, b.x, b.y, b.x + b.w, b.y + b.h);
        int border = argb(200, 60, 200, 80);

        ctx.fill(b.x, b.y, b.x + b.w, b.y + b.h, argb(active || hovered ? 200 : 160, 10, 60, 20));
        ctx.fill(b.x, b.y, b.x + b.w, b.y + 1, border);
        ctx.fill(b.x, b.y + b.h - 1, b.x + b.w, b.y + b.h, border);
        ctx.fill(b.x, b.y, b.x + 1, b.y + b.h, border);
        ctx.fill(b.x + b.w - 1, b.y, b.x + b.w, b.y + b.h, border);
        ctx.drawString(font, "✚ Healing Mode", b.x + 6, b.y + 4, argb(255, 80, 255, 120));

        if (active || hovered)
            ctx.drawCenteredString(font, active ? "§e[Shaman HUD]" : "§7[Shaman HUD — drag]",
                b.x + b.w / 2, b.y - 11, 0xFFFFFF);
    }

    // Bounds helpers

    private record SkillBarBounds(int x, int y, int w, int h) {}
    private record StunBounds(int x, int y, int w, int h) {}
    private record ClassBounds(int x, int y, int w, int h) {}
    private record ShamanBounds(int x, int y, int w, int h) {}

    private SkillBarBounds skillBarBounds(StunConfig cfg, int sw, int sh) {
        float scale = Math.max(0.5f, Math.min(2.0f, cfg.skillBarScale));
        int sw2  = (int)(SkillBarRenderer.SLOT_W * scale);
        int sh2  = (int)(SkillBarRenderer.SLOT_H * scale);
        int gap2 = (int)(SkillBarRenderer.GAP * scale);
        int bw = SkillBarRenderer.SLOT_COUNT * (sw2 + gap2) - gap2;
        int bh = sh2 + (int)(12 * scale);
        int x = cfg.skillBarX == 0 && cfg.skillBarY == 0
            ? (sw - bw) / 2 : Math.max(0, Math.min(sw - bw, cfg.skillBarX));
        int y = cfg.skillBarX == 0 && cfg.skillBarY == 0
            ? sh - bh - 5 : Math.max(0, Math.min(sh - bh, cfg.skillBarY));
        return new SkillBarBounds(x, y, bw, bh);
    }

    private StunBounds stunBounds(StunConfig cfg) {
        Font font = Minecraft.getInstance().font;
        int pw = font.width("⚡ STUN READY") + 12;
        int ph = 9 + 8;
        return new StunBounds(cfg.stunHudX, cfg.stunHudY, pw, ph);
    }

    private ClassBounds classBounds(StunConfig cfg, Minecraft mc) {
        Font font = mc.font;
        float scale = Math.max(0.5f, Math.min(2.0f, cfg.classHudScale));
        ClassDetector.PlayerClass cls = ClassDetector.getCurrentClass();
        String label = cls != ClassDetector.PlayerClass.NONE ? cls.displayName() : "Awakened Assassin";
        int rawW = 16 + 4 + font.width(label) + 8;
        int rawH = Math.max(16, 9) + 6;
        return new ClassBounds(cfg.classHudX, cfg.classHudY, (int)(rawW * scale), (int)(rawH * scale));
    }

    private ShamanBounds shamanBounds(StunConfig cfg, Minecraft mc) {
        Font font = mc.font;
        float scale = Math.max(0.5f, Math.min(2.0f, cfg.shamanHudScale));
        int pw = (int)((font.width("✚ Healing Mode") + 12) * scale);
        int ph = (int)((9 + 8) * scale);
        return new ShamanBounds(cfg.shamanHudX, cfg.shamanHudY, pw, ph);
    }

    // Utilities 

    private static boolean hit(int mx, int my, int x1, int y1, int x2, int y2) {
        return mx >= x1 && mx <= x2 && my >= y1 && my <= y2;
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    @Override public void onClose() { save(); Minecraft.getInstance().setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }

    private void save() {
        me.shedaniel.autoconfig.AutoConfig.getConfigHolder(StunConfig.class).save();
    }

    private static int argb(int a, int r, int g, int b) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }
}
