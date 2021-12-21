package me.friwi.jcefmavenbot.qualitycontrol;

import me.friwi.jcefmavenbot.issues.EnumPlatform;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class QualityControl {
    public static List[] generateTestReport(String buildMetaUrl) throws IOException, ParseException {
        List[] ret = new List[EnumPlatform.values().length];
        //Read build meta
        JSONObject build_meta = (JSONObject) new JSONParser().parse(new InputStreamReader(new URL(buildMetaUrl).openConnection().getInputStream()));
        //Start generating artifact lists
        int i = 0;
        for(EnumPlatform platform : EnumPlatform.values()){
            System.out.println("Checking artifact for "+platform+"...");
            //Load local cached list
            BuildArtifactList valid = BuildArtifactListReader.readList(QualityControl.class.getResourceAsStream("/artifact_list_"+platform.name().toLowerCase(Locale.ENGLISH)+".txt"));
            //Generate list for artifact
            String url = (String) build_meta.get("download_url_"+platform.name().toLowerCase(Locale.ENGLISH));
            BuildArtifactList gen = BuildArtifactListGenerator.generate(url);
            //Validate
            ret[i] = valid.validate(gen);
            i++;
        }
        return ret;
    }

    public static JSONObject formatTestReport(List[] report){
        JSONObject object = new JSONObject();
        boolean all_passed = true;
        int i = 0;
        for(EnumPlatform platform : EnumPlatform.values()){
            JSONArray elements = new JSONArray();
            for(Object el : report[i]){
                all_passed = false;
                JSONObject obj = new JSONObject();
                obj.put("path", ((BuildArtifactElement)el).getPath());
                obj.put("reason", ((BuildArtifactElement)el).getFlagReason());
                elements.add(obj);
            }
            object.put("errors_"+platform.name().toLowerCase(Locale.ENGLISH), elements);
            i++;
        }
        object.put("all_passed", all_passed);
        return object;
    }

    public static boolean isOK(List[] report){
        for(List rep : report){
            if(!rep.isEmpty())return false;
        }
        return true;
    }

    public static String generateWarning(List[] report, String issue, String issueUrl){
        String build = ">**WARNING: A part of this build appears to be broken!**\nSee test_report.json and [#"+issue+"]("+issueUrl+") for more info!\n";
        build+="Affected platforms: ";
        int i = 0;
        List<EnumPlatform> affected = new LinkedList<>();
        for(EnumPlatform platform : EnumPlatform.values()){
            if(!report[i].isEmpty()){
                affected.add(platform);
            }
            i++;
        }
        build += Arrays.toString(affected.toArray(new EnumPlatform[0])) + "\n\n";
        return build;
    }
}
