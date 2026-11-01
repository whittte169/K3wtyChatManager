package com.white.k3wtychatmanager.api.chat;

/**
 * 
 * ChatType
 * @author <a href="https://github.com/whittte169">whittte169</a>
 * @version 1.0.0
 * 
 */
public enum ChatType {
    UNDEFINED(0),
    LOCAL(1),
    GLOBAL(2);

    private final int ordinal;
    private static ChatType[] VALUES = values();

    ChatType(int ordinal) {
        this.ordinal = ordinal;
    }
    public int getOrdinal() {
        return ordinal;
    }
    public static ChatType fromOrdinal(int ordinal) {
        return VALUES[ordinal];
    }
}
