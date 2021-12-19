package me.friwi.jcefmavenbot.issues;

import java.util.Locale;

public enum EnumTestResult {
    UNTESTED("Untested", "lightgrey"),
    WORKING("Working", "success"),
    BROKEN("Broken", "critical"),
    CONFLICT("Conflict", "yellow");

    private String value;
    private String badgeColor;

    EnumTestResult(String value, String badgeColor) {
        this.value = value;
        this.badgeColor = badgeColor;
    }

    public String getValue() {
        return value;
    }

    public String getBadgeColor() {
        return badgeColor;
    }

    public static EnumTestResult fromString(String str){
        if(str==null)return null;
        for(EnumTestResult r : values()){
            if(r.getValue().equalsIgnoreCase(str))return r;
        }
        return null;
    }
}
