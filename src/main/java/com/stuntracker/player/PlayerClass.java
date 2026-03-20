package com.stuntracker.player;

/**
 * All known classes on the server.
 * Add more here as needed, matching is case-insensitive substring.
 */
public enum PlayerClass {

    ASSASSIN        ("assassin"),
    MAGE            ("mage"),
    WARRIOR         ("warrior"),
    ARCHER          ("archer"),
    HEALER          ("healer"),
    ROGUE           ("rogue"),
    BERSERKER       ("berserker"),
    PALADIN         ("paladin"),
    NECROMANCER     ("necromancer"),
    UNKNOWN         ("");

    private final String keyword;

    PlayerClass(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Tries to detect a class from an item name string.
     * e.g. "[1] Profile n1 Awakened Assassin Class" → ASSASSIN (from dawnmc hehueuhe)
     *
     * @return matched PlayerClass, or null if no match found
     */
    public static PlayerClass fromItemName(String name) {
        if (name == null) return null;
        String lower = name.toLowerCase();
        for (PlayerClass cls : values()) {
            if (cls == UNKNOWN) continue;
            if (!cls.keyword.isEmpty() && lower.contains(cls.keyword)) {
                return cls;
            }
        }
        return null;
    }

    public String getDisplayName() {
        String n = name().charAt(0) + name().substring(1).toLowerCase();
        return n;
    }
}
