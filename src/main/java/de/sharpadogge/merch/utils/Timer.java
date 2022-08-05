package de.sharpadogge.merch.utils;

public class Timer {
    private long unixTs;

    public Timer() {
        this.unixTs = System.currentTimeMillis();
    }

    public void reset() {
        this.unixTs = System.currentTimeMillis();
    }

    public boolean checkAndReset(final long millis) {
        if ((System.currentTimeMillis() - this.unixTs) >= millis) {
            reset();
            return true;
        }
        return false;
    }
}
