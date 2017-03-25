package com.havrylyuk.elevation.util;

/**
 * Created by Igor Havrylyuk on 25.03.2017.
 */

public enum SourceType {

    SRTM1("Srtm1"),
    SRTM3("Srtm3"),
    ASTERGDEM("Astergdem"),
    GTOPO30("Gtopo30");

    private String name;

    SourceType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static SourceType getByName(String name) {
        for(SourceType e : values()) {
            if(e.name.equals(name)) return e;
        }
        return null;
    }

    public static String[] names() {
        SourceType[] apiTypes = values();
        String[] names = new String[apiTypes.length];
        for (int i = 0; i < apiTypes.length; i++) {
            names[i] = apiTypes[i].name();
        }
        return names;
    }
}
