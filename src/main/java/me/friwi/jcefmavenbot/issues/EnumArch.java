package me.friwi.jcefmavenbot.issues;

public enum EnumArch {
    AMD64("amd64"),
    ARM64("arm64"),
    I386("i386"),
    ARM("arm");

    private String value;

    EnumArch(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static EnumArch fromString(String str){
        if(str==null)return null;
        for(EnumArch r : values()){
            if(r.getValue().equals(str))return r;
        }
        return null;
    }
}
