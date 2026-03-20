package com.stuntracker.skill;

import com.stuntracker.ClassDetector;
import com.stuntracker.config.StunConfig;
import com.stuntracker.shaman.ShamanModeTracker;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class SkillKeyHandler {

    private static final boolean[] prevSkillKeys = new boolean[6]; // Skill1-6 (maps to server slots 0-5, Ultimate=slot6)
    private static boolean prevSkillModeKey = false;

    private SkillKeyHandler() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(SkillKeyHandler::tick);
    }

    private static void tick(Minecraft mc) {
        if (mc.player == null || mc.screen != null) return;

        StunConfig cfg = StunConfig.get();
        if (!cfg.enabled) {
            if (SkillCaster.isSkillModeActive()) SkillCaster.exitSkillMode();
            return;
        }

        long window = GLFW.glfwGetCurrentContext();
        if (window == 0L) return;

        // Manual skill mode toggle (UI only shows/hides the skill overlay)
        if (cfg.skillModeKey == GLFW.GLFW_KEY_UNKNOWN) { prevSkillModeKey = false; }
        boolean modeKeyDown = cfg.skillModeKey != GLFW.GLFW_KEY_UNKNOWN
                && GLFW.glfwGetKey(window, cfg.skillModeKey) == GLFW.GLFW_PRESS;
        boolean modeKeyJustPressed = modeKeyDown && !prevSkillModeKey;
        if (modeKeyJustPressed) {
            SkillCaster.toggleSkillMode();
        }
        prevSkillModeKey = modeKeyDown;

        // Skill keys each press casts immediately (slot switch + offhand packet)
        int[] skillKeys = cfg.getSkillKeys();

        for (int i = 0; i < skillKeys.length; i++) {
            if (skillKeys[i] == GLFW.GLFW_KEY_UNKNOWN) {
                prevSkillKeys[i] = false;
                continue;
            }

            boolean keyDown = GLFW.glfwGetKey(window, skillKeys[i]) == GLFW.GLFW_PRESS;

            if (keyDown && !prevSkillKeys[i]) {
                // Skip if this key is also the skill mode toggle
                if (skillKeys[i] == cfg.skillModeKey) {
                    prevSkillKeys[i] = keyDown;
                    continue;
                }

                // Cast the skill i=0 → server slot 0 (Skill1), ..., i=6 → slot 6 (Ultimate)
                SkillCaster.castSkill(i);

                // Notify Shaman tracker when Skill 1 (slot 0) is cast
                if (i == 0 && ClassDetector.getCurrentClass() == ClassDetector.PlayerClass.AWAKENED_SHAMAN) {
                    ShamanModeTracker.onSkill1Cast();
                }
            }

            prevSkillKeys[i] = keyDown;
        }
    }
}
