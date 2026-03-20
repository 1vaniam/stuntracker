package com.stuntracker.skill;

/**
 * Stub, the skill mode overlay HUD has been removed.
 * lastCastSlot and lastCastMs are kept because SkillBarRenderer and HudLayoutScreen
 * use them to flash the active slot in the skill bar.
 */
public final class SkillHudRenderer {

    public static int lastCastSlot = -1;
    public static long lastCastMs  = 0;

    private SkillHudRenderer() {}

    /** Non op overlay has been removed. */
    public static void register() {}

    /** Called by SkillCaster when a skill is cast to trigger the flash on the skill bar. */
    public static void notifyCast(int slot) {
        lastCastSlot = slot;
        lastCastMs   = System.currentTimeMillis();
    }
}
