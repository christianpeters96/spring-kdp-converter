package de.sharpadogge.merch.utils;

public class Counter {
    private int value = 0;

    public Counter() {
    }

    public Counter(int value) {
        this.value = value;
    }

    public int inc() {
        return ++value;
    }

    public int get() {
        return value;
    }
}
