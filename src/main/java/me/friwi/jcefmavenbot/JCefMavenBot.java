package me.friwi.jcefmavenbot;

import me.friwi.jcefmavenbot.buildissuer.BuildIssuer;
import me.friwi.jcefmavenbot.github.GitHubRequestAuthorization;
import me.friwi.jcefmavenbot.issues.HttpURLConnectionPatch;
import me.friwi.jcefmavenbot.issues.IssueModule;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class JCefMavenBot {
    public static JSONObject CONFIG;

    public static void main(String[] args) throws IOException, ParseException, NoSuchFieldException, IllegalAccessException {
        //Allow http patch
        //Only works up to java 11
        HttpURLConnectionPatch.allowPatchMethod();

        //Load config
        CONFIG = (JSONObject) new JSONParser().parse(new InputStreamReader(new FileInputStream("config.json")));

        //Set auth info
        GitHubRequestAuthorization.init((String) CONFIG.get("botUser"), (String) CONFIG.get("botPAT"));

        int modules = 0;

        //Run build module
        if((Boolean)CONFIG.get("enableBuildModule")){
            modules++;
            BuildIssuer.issueBuilds();
            System.out.println();
        }

        //Run issue module
        if((Boolean)CONFIG.get("enableIssueModule")){
            modules++;
            IssueModule.processIssues();
        }

        if(modules==0){
            System.out.println("Warning: Had nothing to do. Please enable a module!");
        }
    }
}
