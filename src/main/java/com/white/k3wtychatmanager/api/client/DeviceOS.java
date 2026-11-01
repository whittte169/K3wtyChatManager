package com.white.k3wtychatmanager.api.client;

/**
 * 
 * DeviceOS
 * need for indicating an OS of player's device
 * @author <a href="https://github.com/whittte169">whittte169</a>
 * 
 */
public enum DeviceOS {
    UNDEFINED(0),
    ANDROID(1),
    IOS(2),
    MACOS(3),
    FIRE(4),
    GEAR_VR(5),
    HOLOLENS(6),
    WINDOWS(7),
    WINDOWS_X86(8),
    DEDICATED(9),
    TVOS(10),
    PLAYSTATION(11),
    SWITCH(12),
    XBOX(13),
    WINDOWS_PHONE(14),
    LINUX(15);

    private final int ordinal;
    private static DeviceOS[] VALUES = values();

    DeviceOS(int ordinal) {
        this.ordinal = ordinal;
    }
    public int getOrdinal() {
        return ordinal;
    }
    public static DeviceOS fromOrdinal(int ordinal) {
        return VALUES[ordinal];
    }
}
