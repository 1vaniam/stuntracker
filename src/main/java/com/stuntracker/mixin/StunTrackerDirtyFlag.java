package com.stuntracker.mixin;

/**
 * Simple volatile flag that the mixin sets when effect packets arrive,
 * and the tick handler reads + clears each tick.
 *
 * This allows the tick handler to skip evaluation on ticks where
 * no relevant packets arrived, saving a tiny bit of CPU on servers
 * with many active players.
 */
public final class StunTrackerDirtyFlag {

    private static volatile boolean dirty = true; // start dirty so first tick evaluates

    private StunTrackerDirtyFlag() {}

    public static void markDirty() {
        dirty = true;
    }

    /** Returns true and clears the flag, or false if not dirty. */
    public static boolean consumeDirty() {
        if (dirty) {
            dirty = false;
            return true;
        }
        return false;
    }
}
