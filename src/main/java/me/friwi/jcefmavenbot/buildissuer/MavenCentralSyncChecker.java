package me.friwi.jcefmavenbot.buildissuer;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MavenCentralSyncChecker {
    public static final String CENTRAL_URL = "https://repo.maven.apache.org/maven2/";

    /**
     * This only checks for jcefmaven and jcef-api artifacts, as they are required to
     * build the jcefsampleapp. Jogl and gluegen-rt are not synced every time, so we
     * do not check for it. The natives will be ready by the time someone runs the
     * sample app.
     * @param mvn_version v1.0.0+jcef...
     * @return true if all are found
     */
    public static boolean checkAllSynced(String mvn_version, String jcef_version){
        if(!checkSynced("me.friwi", "jcefmaven", mvn_version))return false;
        if(!checkSynced("me.friwi", "jcef-api", jcef_version))return false;
        return true;
    }

    private static boolean checkSynced(String groupId, String artifactId, String version){
        try {
            URLConnection connection = new URL(
                    CENTRAL_URL+groupId.replace(".", "/")+
                            "/"+artifactId+"/"+version+"/"+artifactId+"-"+version+".jar")
                    .openConnection();
            try(InputStream ignored = connection.getInputStream()){
                return true;
            }catch (Exception e){
                return false;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}
