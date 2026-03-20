package com.stuntracker.hud;

import com.stuntracker.ClassDetector;
import com.stuntracker.config.StunConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ClassHudEditScreen extends Screen {

    private final Screen parent;
    private boolean dragging = false;
    private int dragOffsetX, dragOffsetY;
    private static final int SNAP_THRESHOLD = 16;
    private static final int PAD_X = 4, PAD_Y = 3, ICON_SIZE = 16;

    public ClassHudEditScreen(Screen parent) {
        super(Component.literal("Edit Class HUD Position"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Done"), btn -> onClose())
            .bounds(this.width / 2 - 50, this.height - 28, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Reset Position"),
                btn -> { StunConfig cfg = StunConfig.get(); cfg.classHudX = 4; cfg.classHudY = 20; save(); })
            .bounds(this.width / 2 - 60, this.height - 52, 120, 20).build());
    }

    private int getPillWidth() {
        String label = ClassDetector.PlayerClass.AWAKENED_ASSASSIN.displayName();
        return ICON_SIZE + PAD_X + Minecraft.getInstance().font.width(label) + PAD_X * 2;
    }
    private int getPillHeight() { return Math.max(ICON_SIZE, 9) + PAD_Y * 2; }

    @Override
    public void tick() {
        super.tick();
        long window = GLFW.glfwGetCurrentContext();
        if (window == 0L) return;

        double[] mx = new double[1], my = new double[1];
        GLFW.glfwGetCursorPos(window, mx, my);
        Minecraft mc = Minecraft.getInstance();
        double scale = mc.getWindow().getGuiScale();
        double mouseX = mx[0] / scale, mouseY = my[0] / scale;
        boolean leftDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        StunConfig cfg = StunConfig.get();
        int pw = getPillWidth(), ph = getPillHeight();
        int sw = mc.getWindow().getGuiScaledWidth(), sh = mc.getWindow().getGuiScaledHeight();

        if (leftDown) {
            if (!dragging) {
                if (mouseX >= cfg.classHudX && mouseX <= cfg.classHudX + pw &&
                    mouseY >= cfg.classHudY && mouseY <= cfg.classHudY + ph) {
                    dragging = true;
                    dragOffsetX = (int)(mouseX - cfg.classHudX);
                    dragOffsetY = (int)(mouseY - cfg.classHudY);
                }
            } else {
                int nx = (int) Math.max(0, Math.min(sw - pw, mouseX - dragOffsetX));
                int ny = (int) Math.max(0, Math.min(sh - ph, mouseY - dragOffsetY));
                // Snap to center
                if (Math.abs(nx - (sw - pw) / 2) <= SNAP_THRESHOLD) nx = (sw - pw) / 2;
                if (Math.abs(ny - (sh - ph) / 2) <= SNAP_THRESHOLD) ny = (sh - ph) / 2;
                cfg.classHudX = nx; cfg.classHudY = ny; save();
            }
        } else { dragging = false; }
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        StunConfig cfg = StunConfig.get();
        Minecraft mc = Minecraft.getInstance();
        int sw = mc.getWindow().getGuiScaledWidth(), sh = mc.getWindow().getGuiScaledHeight();
        int pw = getPillWidth(), ph = getPillHeight();
        int x = cfg.classHudX, y = cfg.classHudY;

        // Snap guide lines
        if (Math.abs(x - (sw - pw) / 2) <= SNAP_THRESHOLD)
            ctx.fill(sw / 2, 0, sw / 2 + 1, sh, argb(80, 255, 255, 0));
        if (Math.abs(y - (sh - ph) / 2) <= SNAP_THRESHOLD)
            ctx.fill(0, sh / 2, sw, sh / 2 + 1, argb(80, 255, 255, 0));

        boolean hovered = mouseX >= x && mouseX <= x + pw && mouseY >= y && mouseY <= y + ph;
        int rim = dragging ? argb(255, 255, 210, 0) : argb(200, 100, 180, 255);

        ctx.fill(x, y, x + pw, y + ph, argb(dragging || hovered ? 220 : 160, 0, 0, 0));
        ctx.fill(x, y, x + pw, y + 1, rim);
        ctx.fill(x, y + ph - 1, x + pw, y + ph, rim);
        ctx.fill(x, y, x + 1, y + ph, rim);
        ctx.fill(x + pw - 1, y, x + pw, y + ph, rim);

        int[] col = { argb(200, 40, 10, 60), argb(255, 180, 80, 220) };
        ctx.fill(x + PAD_X, y + PAD_Y, x + PAD_X + ICON_SIZE, y + PAD_Y + ICON_SIZE, col[0]);
        ctx.fill(x + PAD_X, y + PAD_Y, x + PAD_X + ICON_SIZE, y + PAD_Y + 1, col[1]);
        ctx.fill(x + PAD_X, y + PAD_Y + ICON_SIZE - 1, x + PAD_X + ICON_SIZE, y + PAD_Y + ICON_SIZE, col[1]);
        ctx.drawString(font, ClassDetector.PlayerClass.AWAKENED_ASSASSIN.displayName(),
            x + PAD_X + ICON_SIZE + PAD_X, y + PAD_Y + (ICON_SIZE - 9) / 2, argb(255, 150, 210, 255));

        ctx.drawCenteredString(font, "§eDrag to reposition  §7(snaps to center)", this.width / 2, 12, 0xFFFFFF);
        if (hovered && !dragging)
            ctx.drawCenteredString(font, "§eClick to drag", x + pw / 2, y + ph + 3, 0xFFFFFF);
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override public void onClose() { save(); Minecraft.getInstance().setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
    private void save() { me.shedaniel.autoconfig.AutoConfig.getConfigHolder(StunConfig.class).save(); }
    private static int argb(int a, int r, int g, int b) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }
}
