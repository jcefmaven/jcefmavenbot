package me.friwi.jcefmavenbot.qualitycontrol;

public class BuildArtifactElement {
    public static final double ACCEPTED_ERROR = 0.5;

    private String path;
    private long sizeEstimate;
    private String flagReason = null;

    public BuildArtifactElement(String path, long sizeEstimate) {
        this.path = path;
        this.sizeEstimate = sizeEstimate;
    }

    public String getPath() {
        return path;
    }

    public long getSizeEstimate() {
        return sizeEstimate;
    }

    public String getFlagReason() {
        return flagReason;
    }

    public void setFlagReason(String flagReason) {
        this.flagReason = flagReason;
    }

    /**
     * Assumes this is the valid element
     * @param other
     * @return
     */
    public boolean validate(BuildArtifactElement other){
        if(other==null)return false;
        return other.path.equals(this.path) && other.sizeEstimate > sizeEstimate*ACCEPTED_ERROR;
    }


}
