package me.friwi.jcefmavenbot;

import me.friwi.jcefmavenbot.buildissuer.BuildIssuer;
import me.friwi.jcefmavenbot.github.GitHubRequestAuthorization;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class JCefMavenBot {
    public static JSONObject CONFIG;

    public static void main(String[] args) throws IOException, ParseException {
        //Load config
        CONFIG = (JSONObject) new JSONParser().parse(new InputStreamReader(new FileInputStream("config.json")));

        //Set auth info
        GitHubRequestAuthorization.init((String) CONFIG.get("botUser"), (String) CONFIG.get("botPAT"));

        int modules = 0;

        //Run build module
        if((Boolean)CONFIG.get("enableBuildModule")){
            modules++;
            BuildIssuer.issueBuilds();
        }

        //Run issue module
        if((Boolean)CONFIG.get("enableIssueModule")){
            modules++;
            //TODO
            System.out.println("################################");
            System.out.println("#         Issue Module         #");
            System.out.println("################################\n");
        }

        if(modules==0){
            System.out.println("Warning: Had nothing to do. Please enable a module!");
        }
    }
}
