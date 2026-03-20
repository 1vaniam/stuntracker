package com.stuntracker;

public final class StunState {

    public enum Status { UNKNOWN, READY, COOLDOWN }

    private static volatile Status current = Status.UNKNOWN;
    private static volatile long lastChangeMs = 0;

    private StunState() {}

    public static Status get() { return current; }
    public static boolean isReady() { return current == Status.READY; }

    public static boolean set(Status next) {
        if (current == next) return false;
        current = next;
        lastChangeMs = System.currentTimeMillis();
        return true;
    }

    public static long msSinceChange() {
        return System.currentTimeMillis() - lastChangeMs;
    }
}
