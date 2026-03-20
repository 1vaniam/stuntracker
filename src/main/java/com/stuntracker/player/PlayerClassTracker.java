package com.stuntracker.player;

/**
 * Holds the player's currently active class.
 * Other parts of the mod (stun tracker, skill caster, HUD) read from here
 * to adapt their behaviour per-class.
 */
public final class PlayerClassTracker {

    private static volatile PlayerClass currentClass = PlayerClass.UNKNOWN;

    private PlayerClassTracker() {}

    public static PlayerClass getCurrentClass() {
        return currentClass;
    }

    public static boolean isClass(PlayerClass cls) {
        return currentClass == cls;
    }

    public static boolean isUnknown() {
        return currentClass == PlayerClass.UNKNOWN;
    }

    public static void setCurrentClass(PlayerClass cls) {
        if (cls == currentClass) return;
        currentClass = cls;
        System.out.println("[SkillKeybind] Class set to: " + cls.getDisplayName());
    }
}
