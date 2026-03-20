package com.stuntracker.shaman;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * Tracks the Shaman's skill 1 toggle state.
 * Default = ATTACK. Each cast of skill 1 flips to the other mode.
 * Resets to ATTACK on disconnect/rejoin.
 */
public final class ShamanModeTracker {

    public enum ShamanMode {
        ATTACK, HEALING;

        public String displayName() {
            return switch (this) {
                case ATTACK  -> "Attack Mode";
                case HEALING -> "Healing Mode";
            };
        }
    }

    private static ShamanMode currentMode = ShamanMode.ATTACK;
    private static boolean wasConnected = false;

    private ShamanModeTracker() {}

    public static ShamanMode getCurrentMode() { return currentMode; }

    public static void onSkill1Cast() {
        currentMode = (currentMode == ShamanMode.ATTACK)
            ? ShamanMode.HEALING
            : ShamanMode.ATTACK;
    }

    public static void reset() {
        currentMode = ShamanMode.ATTACK;
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            boolean connected = mc.player != null && mc.getConnection() != null;
            if (wasConnected && !connected) {
                reset();
            }
            wasConnected = connected;
        });
    }
}
