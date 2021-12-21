package me.friwi.jcefmavenbot.qualitycontrol;

import java.util.ArrayList;
import java.util.List;

public class BuildArtifactList {
    private List<BuildArtifactElement> buildContents = new ArrayList<>(200);

    protected BuildArtifactList() {

    }

    protected void addElement(BuildArtifactElement element){
        this.buildContents.add(element);
    }

    @Override
    public String toString(){
        StringBuilder x = new StringBuilder();
        for(BuildArtifactElement el : buildContents){
            x.append(el.getSizeEstimate()).append(":").append(el.getPath()).append("\n");
        }
        return x.toString();
    }

    /**
     * Assumes this is the correct artifact list
     * @param other
     * @return
     */
    public List<BuildArtifactElement> validate(BuildArtifactList other){
        List<BuildArtifactElement> invalid = new ArrayList<>();
        for(BuildArtifactElement element : this.buildContents){
            //Check if element exists in other
            BuildArtifactElement found = null;
            for(BuildArtifactElement el1 : other.buildContents){
                if(el1.getPath().equals(element.getPath())){
                    found = el1;
                    break;
                }
            }
            if(found==null){
                element.setFlagReason("is missing");
                invalid.add(element);
            }else{
                if(!element.validate(found)){
                    element.setFlagReason("is too small: "+formatBytes(found.getSizeEstimate())+" (expected ~"+formatBytes(element.getSizeEstimate())+")");
                }
            }
        }
        return invalid;
    }

    private String formatBytes(double bytes) {
        if(bytes>1024){
            bytes/=1024;
            if(bytes>1024){
                bytes/=1024;
                return String.format("%.2fMB", bytes);
            }else{
                return String.format("%.2fKB", bytes);
            }
        }else{
            return String.format("%.2fB", bytes);
        }
    }
}
