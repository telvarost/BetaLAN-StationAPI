package com.github.telvarost.betalan.util;

public enum DefaultGamemodeEnum {
    SURVIVAL("Survival"),
    CREATIVE("Creative");

    final String stringValue;

    DefaultGamemodeEnum() {
        this.stringValue = "Survival";
    }

    DefaultGamemodeEnum(String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public String toString() {
        return stringValue;
    }
}