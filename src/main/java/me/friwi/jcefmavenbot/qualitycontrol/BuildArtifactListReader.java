package me.friwi.jcefmavenbot.qualitycontrol;

import java.io.*;

public class BuildArtifactListReader {
    public static BuildArtifactList readList(InputStream in) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        BuildArtifactList list = new BuildArtifactList();
        String s;
        while((s=r.readLine())!=null){
            String[] p = s.split(":");
            if(p.length<2)continue;
            list.addElement(new BuildArtifactElement(p[1].trim(), Long.parseLong(p[0].trim())));
        }
        r.close();
        return list;
    }
}
