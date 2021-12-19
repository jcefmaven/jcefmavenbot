package me.friwi.jcefmavenbot.issues;

public enum EnumOS {
    //In order of table
    LINUX("linux"),
    WINDOWS("windows"),
    MACOSX("macosx");

    private String value;

    EnumOS(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static EnumOS fromString(String str){
        if(str==null)return null;
        for(EnumOS r : values()){
            if(r.getValue().equals(str))return r;
        }
        return null;
    }
}
