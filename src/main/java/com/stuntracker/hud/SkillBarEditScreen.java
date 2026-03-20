package com.stuntracker.hud;

import com.stuntracker.config.StunConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class SkillBarEditScreen extends Screen {

    private final Screen parent;
    private boolean dragging = false;
    private int dragOffsetX, dragOffsetY;

    // Snap threshold if within this many pixels of center, snap
    private static final int SNAP_THRESHOLD = 16;

    private static final String[] LABELS   = {"P", "1", "2", "3", "4", "5", "U"};
    private static final String[] TOOLTIPS = {"Passive","Skill 1","Skill 2","Skill 3","Skill 4","Skill 5","Ultimate"};

    public SkillBarEditScreen(Screen parent) {
        super(Component.literal("Edit Skill Bar Position"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(
                Component.literal("Done"), btn -> onClose())
            .bounds(this.width / 2 - 50, this.height - 28, 100, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("Reset Position"),
                btn -> {
                    StunConfig cfg = StunConfig.get();
                    cfg.skillBarX = (this.width - SkillBarRenderer.BAR_W) / 2;
                    cfg.skillBarY = this.height - 60;
                    save();
                })
            .bounds(this.width / 2 - 60, this.height - 52, 120, 20).build());
    }

    @Override
    public void tick() {
        super.tick();
        long window = GLFW.glfwGetCurrentContext();
        if (window == 0L) return;

        double[] mx = new double[1], my = new double[1];
        GLFW.glfwGetCursorPos(window, mx, my);

        Minecraft mc = Minecraft.getInstance();
        double scale = mc.getWindow().getGuiScale();
        double mouseX = mx[0] / scale;
        double mouseY = my[0] / scale;

        boolean leftDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        StunConfig cfg = StunConfig.get();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        int bw = SkillBarRenderer.BAR_W;
        int bh = SkillBarRenderer.BAR_H;
        int px = cfg.skillBarX;
        int py = cfg.skillBarY;

        if (leftDown) {
            if (!dragging) {
                if (mouseX >= px - 3 && mouseX <= px + bw + 3 &&
                    mouseY >= py - 3 && mouseY <= py + bh + 3) {
                    dragging = true;
                    dragOffsetX = (int)(mouseX - px);
                    dragOffsetY = (int)(mouseY - py);
                }
            } else {
                int newX = (int) Math.max(0, Math.min(sw - bw, mouseX - dragOffsetX));
                int newY = (int) Math.max(0, Math.min(sh - bh, mouseY - dragOffsetY));

                // Snap to horizontal center
                int centerX = (sw - bw) / 2;
                if (Math.abs(newX - centerX) <= SNAP_THRESHOLD) newX = centerX;

                // Snap to vertical center
                int centerY = (sh - bh) / 2;
                if (Math.abs(newY - centerY) <= SNAP_THRESHOLD) newY = centerY;

                cfg.skillBarX = newX;
                cfg.skillBarY = newY;
                save();
            }
        } else {
            dragging = false;
        }
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        StunConfig cfg = StunConfig.get();
        Minecraft mc = Minecraft.getInstance();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        int x = cfg.skillBarX;
        int y = cfg.skillBarY;
        int bw = SkillBarRenderer.BAR_W;
        int bh = SkillBarRenderer.BAR_H;

        boolean hovered = mouseX >= x - 3 && mouseX <= x + bw + 3 &&
                          mouseY >= y - 3 && mouseY <= y + bh + 3;

        // Snap guide lines
        int centerX = (sw - bw) / 2;
        int centerY = (sh - bh) / 2;
        if (Math.abs(x - centerX) <= SNAP_THRESHOLD) {
            ctx.fill(sw / 2, 0, sw / 2 + 1, sh, argb(80, 255, 255, 0)); // vertical center line
        }
        if (Math.abs(y - centerY) <= SNAP_THRESHOLD) {
            ctx.fill(0, sh / 2, sw, sh / 2 + 1, argb(80, 255, 255, 0)); // horizontal center line
        }

        // Draw the skill bar preview
        ctx.fill(x - 3, y - 3, x + bw + 3, y + bh + 3,
            argb(dragging ? 200 : (hovered ? 180 : 160), 0, 0, 0));
        ctx.fill(x - 3, y - 3, x + bw + 3, y - 2,
            argb(dragging ? 255 : 180, dragging ? 255 : 80, dragging ? 210 : 80, 0));

        Font font = mc.font;
        for (int i = 0; i < SkillBarRenderer.SLOT_COUNT; i++) {
            int sx = x + i * (SkillBarRenderer.SLOT_W + SkillBarRenderer.GAP);
            int sy = y;
            int sw2 = SkillBarRenderer.SLOT_W;
            int sh2 = SkillBarRenderer.SLOT_H;

            boolean isPassive = i == 0;
            boolean isUlt = i == 6;
            int bg = isPassive ? argb(180, 20, 30, 80) : isUlt ? argb(180, 80, 40, 0) : argb(180, 40, 40, 40);
            int border = isPassive ? argb(200, 60, 90, 180) : isUlt ? argb(200, 180, 120, 0) : argb(200, 100, 100, 100);
            int tc = isPassive ? argb(200, 140, 170, 255) : isUlt ? argb(200, 255, 180, 50) : argb(200, 210, 210, 210);

            ctx.fill(sx, sy, sx + sw2, sy + sh2, bg);
            ctx.fill(sx, sy, sx + sw2, sy + 1, border);
            ctx.fill(sx, sy + sh2-1, sx + sw2, sy + sh2, border);
            ctx.fill(sx, sy, sx + 1, sy + sh2, border);
            ctx.fill(sx + sw2-1, sy, sx + sw2, sy + sh2, border);

            int lw = font.width(LABELS[i]);
            ctx.drawString(font, LABELS[i], sx + (sw2 - lw) / 2, sy + 7, tc);

            int tw = font.width(TOOLTIPS[i]);
            ctx.drawString(font, TOOLTIPS[i], sx + (sw2 - tw) / 2, sy + sh2 + 2, argb(160, 180, 180, 180));
        }

        // Instructions
        ctx.drawCenteredString(font, "§eDrag the skill bar to reposition", sw / 2, 12, 0xFFFFFF);
        ctx.drawCenteredString(font, "§7Snaps to center within " + SNAP_THRESHOLD + " pixels", sw / 2, 24, 0xAAAAAA);
        if (hovered && !dragging)
            ctx.drawCenteredString(font, "§eClick to drag", x + bw / 2, y + bh + 6, 0xFFFFFF);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() { save(); Minecraft.getInstance().setScreen(parent); }

    @Override
    public boolean isPauseScreen() { return false; }

    private void save() {
        me.shedaniel.autoconfig.AutoConfig.getConfigHolder(StunConfig.class).save();
    }

    private static int argb(int a, int r, int g, int b) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    // Inner class for font access
}
