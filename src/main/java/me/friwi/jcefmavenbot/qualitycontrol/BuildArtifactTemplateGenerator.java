package me.friwi.jcefmavenbot.qualitycontrol;

import me.friwi.jcefmavenbot.issues.EnumPlatform;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class BuildArtifactTemplateGenerator {
    public static void main(String args[]) throws IOException, ParseException {
        //Generates the templates used for future checking
        if(args.length==0){
            System.err.println("Please supply the build meta url as an argument!");
            System.exit(1);
        }
        //Read build meta
        JSONObject build_meta = (JSONObject) new JSONParser().parse(new InputStreamReader(new URL(args[0]).openConnection().getInputStream()));
        //Start generating artifact lists
        for(EnumPlatform platform : EnumPlatform.values()){
            System.out.println("Generating list for "+platform);
            File dest = new File("src/main/resources/artifact_list_"+platform.name().toLowerCase(Locale.ENGLISH)+".txt");
            String url = (String) build_meta.get("download_url_"+platform.name().toLowerCase(Locale.ENGLISH));
            BuildArtifactList list = BuildArtifactListGenerator.generate(url);
            if(!dest.exists()){
                dest.getParentFile().mkdirs();
                dest.createNewFile();
            }
            PrintWriter out = new PrintWriter(new FileOutputStream(dest));
            out.write(list.toString());
            out.flush();
            out.close();
        }
    }
}
