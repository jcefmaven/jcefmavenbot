package me.friwi.jcefmavenbot.buildissuer;

public class SemanticVersion {
    private int major, minor, patch, prerelease;

    private SemanticVersion(int major, int minor, int patch, int prerelease) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.prerelease = prerelease;
    }

    public static SemanticVersion fromString(String str){
        String[] parts = str.split("\\.");
        if(parts.length==3){
            return new SemanticVersion(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), 0);
        }else if(parts.length==4){
            return new SemanticVersion(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
        }else{
            throw new RuntimeException("Invalid semver: "+str);
        }
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    public int getPrerelease() {
        return prerelease;
    }

    public boolean isSamePatch(SemanticVersion other){
        if(other==null)return false;
        return other.getMajor()==getMajor() && other.getMinor()==getMinor() && other.getPatch()==getPatch();
    }

    public void increasePrerelease(){
        prerelease++;
    }

    @Override
    public String toString() {
        return major+"."+minor+"."+patch+(prerelease>0?"."+prerelease:"");
    }
}
