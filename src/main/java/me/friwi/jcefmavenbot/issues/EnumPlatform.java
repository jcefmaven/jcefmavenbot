package me.friwi.jcefmavenbot.issues;

public enum EnumPlatform {
    LINUX_AMD64(EnumOS.LINUX, EnumArch.AMD64),
    LINUX_ARM64(EnumOS.LINUX, EnumArch.ARM64),
    LINUX_I386(EnumOS.LINUX, EnumArch.I386),
    LINUX_ARM(EnumOS.LINUX, EnumArch.ARM),

    WINDOWS_AMD64(EnumOS.WINDOWS, EnumArch.AMD64),
    WINDOWS_ARM64(EnumOS.WINDOWS, EnumArch.ARM64),
    WINDOWS_I386(EnumOS.WINDOWS, EnumArch.I386),

    MACOSX_AMD64(EnumOS.MACOSX, EnumArch.AMD64),
    MACOSX_ARM64(EnumOS.MACOSX, EnumArch.ARM64);

    private EnumOS os;
    private EnumArch arch;

    EnumPlatform(EnumOS os, EnumArch arch) {
        this.os = os;
        this.arch = arch;
    }

    public EnumOS getOS() {
        return os;
    }

    public EnumArch getArch() {
        return arch;
    }

    public static EnumPlatform fromOSAndArch(EnumOS os, EnumArch arch){
        if(os==null||arch==null)return null;
        for(EnumPlatform r : values()){
            if(r.getOS()==os && r.getArch()==arch)return r;
        }
        return null;
    }
}
