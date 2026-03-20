package com.stuntracker.config;

import com.stuntracker.hud.HudLayoutScreen;
import com.stuntracker.skill.SkillSyncer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class StunConfigScreen extends Screen {

    private final Screen parent;
    private int recordingIndex = -1;

    private static final String[] SKILL_LABELS = {
        "Skill 1", "Skill 2", "Skill 3", "Skill 4", "Skill 5", "Skill 6 (Ultimate)"
    };

    private static final int ROW_H    = 24;
    private static final int BTN_H    = 20;
    private static final int HEADER_H = 34;
    private static final int FOOTER_H = 36;

    // Scrollable button list: each entry stores the button and its fixed virtual Y
    private static class Row { Button btn; int virtualY; Row(Button b, int y) { btn = b; virtualY = y; } }
    private final List<Row> rows = new ArrayList<>();

    private final List<Button> bindButtons = new ArrayList<>();
    private Button skillModeBindButton;
    private Button enabledButton, showHudButton, readySoundButton;
    private Button syncSkillButton;
    private Button cooldownSoundButton, debugButton, filterButton;
    private Button showClassHudButton, showShamanHudButton, showSkillBarButton;
    private Button filterEndPortalButton, filterExtinguishButton;

    private int scrollY    = 0;
    private int totalH     = 0;

    private final boolean[] prevKeyState = new boolean[GLFW.GLFW_KEY_LAST + 1];

    public StunConfigScreen(Screen parent) {
        super(Component.literal("SkillKeybind Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        rows.clear();
        bindButtons.clear();
        scrollY = 0;
        StunConfig cfg = StunConfig.get();

        // Content width fills the screen minus 20px margins each side, capped them & 260
        int cw = Math.min(260, this.width - 40);
        int cx = this.width / 2;
        int x  = cx - cw / 2;
        int y  = 0;

        enabledButton = row(Button.builder(toggleLabel("Enabled", cfg.enabled),
                btn -> { cfg.enabled = !cfg.enabled; save(); refreshToggles(); })
            .bounds(x, 0, cw, BTN_H).build(), y);
        y += ROW_H;

        // HUD rows: [toggle — cw-60] [- 26] [+ 26]
        int tw = cw - 56, sx = x + tw + 4;
        showHudButton = row(Button.builder(toggleLabel("Stun HUD [Assassin]", cfg.showHud),
                btn -> { cfg.showHud = !cfg.showHud; save(); refreshToggles(); })
            .bounds(x, 0, tw, BTN_H).build(), y);
        row(Button.builder(Component.literal("-"),
                btn -> { cfg.stunHudScale = Math.max(0.5f, cfg.stunHudScale - 0.1f); save(); })
            .bounds(sx, 0, 26, BTN_H).build(), y);
        row(Button.builder(Component.literal("+"),
                btn -> { cfg.stunHudScale = Math.min(2.0f, cfg.stunHudScale + 0.1f); save(); })
            .bounds(sx + 28, 0, 26, BTN_H).build(), y);
        y += ROW_H;

        int hw = (cw - 2) / 2;
        readySoundButton = row(Button.builder(toggleLabel("Ready Sound", cfg.playReadySound),
                btn -> { cfg.playReadySound = !cfg.playReadySound; save(); refreshToggles(); })
            .bounds(x, 0, hw, BTN_H).build(), y);
        cooldownSoundButton = row(Button.builder(toggleLabel("CD Sound", cfg.playCooldownSound),
                btn -> { cfg.playCooldownSound = !cfg.playCooldownSound; save(); refreshToggles(); })
            .bounds(x + hw + 2, 0, hw, BTN_H).build(), y);
        y += ROW_H;

        showShamanHudButton = row(Button.builder(toggleLabel("Shaman HUD [Shaman]", cfg.showShamanHud),
                btn -> { cfg.showShamanHud = !cfg.showShamanHud; save(); refreshToggles(); })
            .bounds(x, 0, tw, BTN_H).build(), y);
        row(Button.builder(Component.literal("-"),
                btn -> { cfg.shamanHudScale = Math.max(0.5f, cfg.shamanHudScale - 0.1f); save(); })
            .bounds(sx, 0, 26, BTN_H).build(), y);
        row(Button.builder(Component.literal("+"),
                btn -> { cfg.shamanHudScale = Math.min(2.0f, cfg.shamanHudScale + 0.1f); save(); })
            .bounds(sx + 28, 0, 26, BTN_H).build(), y);
        y += ROW_H;

        showClassHudButton = row(Button.builder(toggleLabel("Class HUD", cfg.showClassHud),
                btn -> { cfg.showClassHud = !cfg.showClassHud; save(); refreshToggles(); })
            .bounds(x, 0, tw, BTN_H).build(), y);
        row(Button.builder(Component.literal("-"),
                btn -> { cfg.classHudScale = Math.max(0.5f, cfg.classHudScale - 0.1f); save(); })
            .bounds(sx, 0, 26, BTN_H).build(), y);
        row(Button.builder(Component.literal("+"),
                btn -> { cfg.classHudScale = Math.min(2.0f, cfg.classHudScale + 0.1f); save(); })
            .bounds(sx + 28, 0, 26, BTN_H).build(), y);
        y += ROW_H;

        showSkillBarButton = row(Button.builder(toggleLabel("Skill Bar HUD", cfg.showSkillBar),
                btn -> { cfg.showSkillBar = !cfg.showSkillBar; save(); refreshToggles(); })
            .bounds(x, 0, tw, BTN_H).build(), y);
        row(Button.builder(Component.literal("-"),
                btn -> { cfg.skillBarScale = Math.max(0.5f, cfg.skillBarScale - 0.1f); save(); })
            .bounds(sx, 0, 26, BTN_H).build(), y);
        row(Button.builder(Component.literal("+"),
                btn -> { cfg.skillBarScale = Math.min(2.0f, cfg.skillBarScale + 0.1f); save(); })
            .bounds(sx + 28, 0, 26, BTN_H).build(), y);
        y += ROW_H;

        row(Button.builder(Component.literal("Edit HUD Layout"),
                btn -> Minecraft.getInstance().setScreen(new HudLayoutScreen(this)))
            .bounds(x, 0, cw, BTN_H).build(), y);
        y += ROW_H + 4;

        filterButton = row(Button.builder(toggleLabel("Filter Spam Messages", cfg.filterSpamMessages),
                btn -> { cfg.filterSpamMessages = !cfg.filterSpamMessages; save(); refreshToggles(); })
            .bounds(x, 0, cw, BTN_H).build(), y);
        y += ROW_H;

        filterEndPortalButton = row(Button.builder(toggleLabel("Filter End Portal Sound", cfg.filterEndPortalSound),
                btn -> { cfg.filterEndPortalSound = !cfg.filterEndPortalSound; save(); refreshToggles(); })
            .bounds(x, 0, cw, BTN_H).build(), y);
        y += ROW_H;

        filterExtinguishButton = row(Button.builder(toggleLabel("Filter Extinguish Sound", cfg.filterExtinguishSound),
                btn -> { cfg.filterExtinguishSound = !cfg.filterExtinguishSound; save(); refreshToggles(); })
            .bounds(x, 0, cw, BTN_H).build(), y);
        y += ROW_H;

        debugButton = row(Button.builder(toggleLabel("Debug Mode", cfg.debugMode),
                btn -> { cfg.debugMode = !cfg.debugMode; save(); refreshToggles(); })
            .bounds(x, 0, cw, BTN_H).build(), y);
        y += ROW_H + 8;

        skillModeBindButton = row(Button.builder(bindLabel("Skill Mode Key", cfg.skillModeKey),
                btn -> startRecording(7))
            .bounds(x, 0, cw, BTN_H).build(), y);
        y += ROW_H;

        int[] keys = cfg.getSkillKeys();
        for (int i = 0; i < SKILL_LABELS.length; i++) {
            final int idx = i;
            Button btn = row(Button.builder(bindLabel(SKILL_LABELS[i], keys[i]),
                    b -> startRecording(idx))
                .bounds(x, 0, cw, BTN_H).build(), y);
            bindButtons.add(btn);
            y += ROW_H;
        }
        y += 6;

        syncSkillButton = row(Button.builder(Component.literal("Sync Skill HUD"),
                btn -> { Minecraft.getInstance().setScreen(null); SkillSyncer.openAndSync(); })
            .bounds(x, 0, cw, BTN_H).build(), y);
        y += ROW_H;

        totalH = y;

        // Done button — fixed, outside scroll
        addRenderableWidget(Button.builder(Component.literal("Done"), btn -> onClose())
            .bounds(cx - 50, this.height - FOOTER_H + 8, 100, BTN_H).build());

        applyScroll();
    }

    /** Add a scrollable button with its fixed virtual Y and immediately register it. */
    private Button row(Button btn, int virtualY) {
        rows.add(new Row(btn, virtualY));
        addRenderableWidget(btn);
        return btn;
    }

    // ── Scroll ────────────────────────────────────────────────────────────────

    private int contentH() { return this.height - HEADER_H - FOOTER_H; }
    private int maxScroll() { return Math.max(0, totalH - contentH()); }

    private void applyScroll() {
        for (Row r : rows) {
            int screenY = HEADER_H + r.virtualY - scrollY;
            r.btn.setY(screenY);
            r.btn.visible = screenY + BTN_H > HEADER_H && screenY < this.height - FOOTER_H;
        }
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double delta) {
        scrollY = clamp(scrollY - (int)(delta * 14), 0, maxScroll());
        applyScroll();
        return true;
    }

    // ── Key recording ─────────────────────────────────────────────────────────

    private void startRecording(int index) {
        recordingIndex = index;
        long window = GLFW.glfwGetCurrentContext();
        for (int k = 0; k <= GLFW.GLFW_KEY_LAST; k++)
            prevKeyState[k] = GLFW.glfwGetKey(window, k) == GLFW.GLFW_PRESS;
        updateBindLabels();
    }

    @Override
    public void tick() {
        super.tick();

        // Handle scrollbar dragging via GLFW (mouse event API changed in 1.21.x)
        if (maxScroll() > 0) {
            long window = org.lwjgl.glfw.GLFW.glfwGetCurrentContext();
            if (window != 0L) {
                boolean leftDown = org.lwjgl.glfw.GLFW.glfwGetMouseButton(window, org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
                if (leftDown) {
                    double[] mx = new double[1], my = new double[1];
                    org.lwjgl.glfw.GLFW.glfwGetCursorPos(window, mx, my);
                    double guiScale = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScale();
                    int mouseX = (int)(mx[0] / guiScale);
                    int mouseY = (int)(my[0] / guiScale);
                    int cw = Math.min(260, this.width - 40);
                    int barX = this.width / 2 + cw / 2 + 4;
                    if (mouseX >= barX && mouseX <= barX + 6) {
                        int trackH = contentH();
                        float ratio = (float)(mouseY - HEADER_H) / trackH;
                        scrollY = clamp((int)(ratio * maxScroll()), 0, maxScroll());
                        applyScroll();
                    }
                }
            }
        }

        if (recordingIndex < 0) return;
        long window = GLFW.glfwGetCurrentContext();
        if (window == 0L) return;
        for (int k = 32; k <= GLFW.GLFW_KEY_LAST; k++) {
            boolean pressed = GLFW.glfwGetKey(window, k) == GLFW.GLFW_PRESS;
            if (pressed && !prevKeyState[k]) {
                if (k == GLFW.GLFW_KEY_ESCAPE) { recordingIndex = -1; updateBindLabels(); }
                else applyKey(k);
                return;
            }
            prevKeyState[k] = pressed;
        }
    }

    private void applyKey(int keyCode) {
        StunConfig cfg = StunConfig.get();
        switch (recordingIndex) {
            case 7 -> cfg.skillModeKey = keyCode;
            case 0 -> cfg.skillKey1 = keyCode;
            case 1 -> cfg.skillKey2 = keyCode;
            case 2 -> cfg.skillKey3 = keyCode;
            case 3 -> cfg.skillKey4 = keyCode;
            case 4 -> cfg.skillKey5 = keyCode;
            case 5 -> cfg.skillKey6 = keyCode;
        }
        save(); recordingIndex = -1; updateBindLabels();
    }

    private void updateBindLabels() {
        StunConfig cfg = StunConfig.get();
        int[] keys = cfg.getSkillKeys();
        if (skillModeBindButton != null)
            skillModeBindButton.setMessage(recordingIndex == 7
                ? Component.literal("[ Press a key... ESC to cancel ]")
                : bindLabel("Skill Mode Key", cfg.skillModeKey));
        for (int i = 0; i < bindButtons.size(); i++)
            bindButtons.get(i).setMessage(recordingIndex == i
                ? Component.literal("[ Press a key... ESC to cancel ]")
                : bindLabel(SKILL_LABELS[i], keys[i]));
    }

    private void refreshToggles() {
        StunConfig cfg = StunConfig.get();
        if (enabledButton       != null) enabledButton.setMessage(toggleLabel("Enabled", cfg.enabled));
        if (showHudButton       != null) showHudButton.setMessage(toggleLabel("Stun HUD [Assassin]", cfg.showHud));
        if (readySoundButton    != null) readySoundButton.setMessage(toggleLabel("Ready Sound", cfg.playReadySound));
        if (cooldownSoundButton != null) cooldownSoundButton.setMessage(toggleLabel("CD Sound", cfg.playCooldownSound));
        if (showShamanHudButton != null) showShamanHudButton.setMessage(toggleLabel("Shaman HUD [Shaman]", cfg.showShamanHud));
        if (showClassHudButton  != null) showClassHudButton.setMessage(toggleLabel("Class HUD", cfg.showClassHud));
        if (showSkillBarButton  != null) showSkillBarButton.setMessage(toggleLabel("Skill Bar HUD", cfg.showSkillBar));
        if (filterButton        != null) filterButton.setMessage(toggleLabel("Filter Spam Messages", cfg.filterSpamMessages));
        if (debugButton             != null) debugButton.setMessage(toggleLabel("Debug Mode", cfg.debugMode));
        if (filterEndPortalButton   != null) filterEndPortalButton.setMessage(toggleLabel("Filter End Portal Sound", cfg.filterEndPortalSound));
        if (filterExtinguishButton  != null) filterExtinguishButton.setMessage(toggleLabel("Filter Extinguish Sound", cfg.filterExtinguishSound));
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        int sw = this.width, sh = this.height;
        int cw = Math.min(260, sw - 40);

        // Dim background
        ctx.fill(0, 0, sw, sh, 0xAA000000);

        // Header / footer masks so buttons clip cleanly
        ctx.fill(0, 0, sw, HEADER_H, 0xDD000000);
        ctx.fill(0, sh - FOOTER_H, sw, sh, 0xDD000000);

        ctx.drawCenteredString(font, "SkillKeybind Config", sw / 2, 8, 0xFFFFFF);
        ctx.drawCenteredString(font, "\u00a77- / +  adjusts HUD scale   |   scroll for more", sw / 2, 20, 0xAAAAAA);

        ctx.fill(sw/2 - cw/2, HEADER_H - 1, sw/2 + cw/2, HEADER_H, 0x55FFFFFF);
        ctx.fill(sw/2 - cw/2, sh - FOOTER_H, sw/2 + cw/2, sh - FOOTER_H + 1, 0x55FFFFFF);

        // Scrollbar
        if (maxScroll() > 0) {
            int barX  = sw/2 + cw/2 + 4;
            int trackH = contentH();
            int thumbH = Math.max(16, (int)((float)trackH / (totalH + FOOTER_H) * trackH));
            int thumbY = HEADER_H + (int)((float)scrollY / maxScroll() * (trackH - thumbH));
            ctx.fill(barX, HEADER_H, barX + 6, sh - FOOTER_H, 0x33FFFFFF);
            ctx.fill(barX, thumbY, barX + 6, thumbY + thumbH, 0xBBFFFFFF);
        }

        if (recordingIndex >= 0)
            ctx.drawCenteredString(font, "\u00a7ePress any key  (ESC to cancel)", sw/2, sh - FOOTER_H + 6, 0xFFFFFF);

        super.render(ctx, mouseX, mouseY, delta);
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }

    private Component toggleLabel(String name, boolean value) {
        return Component.literal(name + ": " + (value ? "\u00a7aON" : "\u00a7cOFF"));
    }

    private Component bindLabel(String name, int keyCode) {
        return Component.literal(name + ": \u00a7a" + keyName(keyCode));
    }

    public static String keyName(int keyCode) {
        if (keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z) return String.valueOf((char) keyCode);
        if (keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) return String.valueOf((char) keyCode);
        return switch (keyCode) {
            case GLFW.GLFW_KEY_UNKNOWN       -> "None";
            case GLFW.GLFW_KEY_SPACE         -> "Space";
            case GLFW.GLFW_KEY_LEFT_SHIFT    -> "Left Shift";
            case GLFW.GLFW_KEY_RIGHT_SHIFT   -> "Right Shift";
            case GLFW.GLFW_KEY_LEFT_CONTROL  -> "Left Ctrl";
            case GLFW.GLFW_KEY_LEFT_ALT      -> "Left Alt";
            case GLFW.GLFW_KEY_TAB           -> "Tab";
            case GLFW.GLFW_KEY_CAPS_LOCK     -> "Caps Lock";
            case GLFW.GLFW_KEY_F1  -> "F1";  case GLFW.GLFW_KEY_F2  -> "F2";
            case GLFW.GLFW_KEY_F3  -> "F3";  case GLFW.GLFW_KEY_F4  -> "F4";
            case GLFW.GLFW_KEY_F5  -> "F5";  case GLFW.GLFW_KEY_F6  -> "F6";
            case GLFW.GLFW_KEY_F7  -> "F7";  case GLFW.GLFW_KEY_F8  -> "F8";
            case GLFW.GLFW_KEY_F9  -> "F9";  case GLFW.GLFW_KEY_F10 -> "F10";
            case GLFW.GLFW_KEY_F11 -> "F11"; case GLFW.GLFW_KEY_F12 -> "F12";
            case GLFW.GLFW_KEY_UP    -> "Up";    case GLFW.GLFW_KEY_DOWN  -> "Down";
            case GLFW.GLFW_KEY_LEFT  -> "Left";  case GLFW.GLFW_KEY_RIGHT -> "Right";
            case GLFW.GLFW_KEY_COMMA         -> ",";  case GLFW.GLFW_KEY_PERIOD       -> ".";
            case GLFW.GLFW_KEY_SLASH         -> "/";  case GLFW.GLFW_KEY_SEMICOLON    -> ";";
            case GLFW.GLFW_KEY_APOSTROPHE    -> "'";  case GLFW.GLFW_KEY_LEFT_BRACKET -> "[";
            case GLFW.GLFW_KEY_RIGHT_BRACKET -> "]";  case GLFW.GLFW_KEY_BACKSLASH    -> "\\";
            case GLFW.GLFW_KEY_MINUS         -> "-";  case GLFW.GLFW_KEY_EQUAL        -> "=";
            default -> "Key " + keyCode;
        };
    }

    @Override public void onClose() { save(); Minecraft.getInstance().setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
    private void save() { me.shedaniel.autoconfig.AutoConfig.getConfigHolder(StunConfig.class).save(); }
}
