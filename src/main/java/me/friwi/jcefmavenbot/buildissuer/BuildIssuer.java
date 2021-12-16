package me.friwi.jcefmavenbot.buildissuer;

import me.friwi.jcefmavenbot.github.api.GitHubAPIDispatchWorkflowRequest;
import me.friwi.jcefmavenbot.github.api.GitHubAPIReleasesRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.*;

import static me.friwi.jcefmavenbot.JCefMavenBot.CONFIG;

public class BuildIssuer {
    public static final int EXPECTED_ASSETS = 12;

    public static void issueBuilds() throws IOException, ParseException {
        System.out.println("################################");
        System.out.println("#         Build Module         #");
        System.out.println("################################\n");

        //Fetch git info
        GitRefResolver.init((String) CONFIG.get("jcefRepo"));
        List<String> commits = GitRefResolver.fetchAllCommitsSince((String) CONFIG.get("initRef"));
        System.out.println("Git commits fetched:");
        System.out.println(Arrays.toString(commits.toArray(new String[0])));

        //Fetch release info of jcefbuild
        JSONArray jcefbuilds = new GitHubAPIReleasesRequest((String) CONFIG.get("jcefBuildRepo")).performRequest(true);
        List<String> jcefbuild_commits = new LinkedList<>();
        Map<String, Integer> commit_to_artifact_amount = new HashMap<>();
        Map<String, String> commit_to_build_meta_url = new HashMap<>();
        String jcefRepoCommitUrl = (String) CONFIG.get("jcefRepoCommitUrl");
        for(Object object : jcefbuilds){
            JSONObject release = (JSONObject) object;
            String body = (String) release.get("body");
            String commit_id = body.substring(body.indexOf(jcefRepoCommitUrl)+jcefRepoCommitUrl.length());
            commit_id = commit_id.substring(0, commit_id.indexOf(")"));
            jcefbuild_commits.add(commit_id);
            JSONArray assets = ((JSONArray) release.get("assets"));
            commit_to_artifact_amount.put(commit_id, assets.size());
            if(assets.size()==EXPECTED_ASSETS) {
                for (Object a : assets) {
                    JSONObject asset = (JSONObject) a;
                    if(asset.get("name").equals("build_meta.json")){
                        commit_to_build_meta_url.put(commit_id, (String) asset.get("browser_download_url"));
                        break;
                    }
                }
            }
        }
        System.out.println();
        System.out.println("jcefbuild releases fetched:");
        System.out.println(Arrays.toString(jcefbuild_commits.toArray(new String[0])));
        for(Map.Entry<String, Integer> ent : commit_to_artifact_amount.entrySet()){
            System.out.println(ent.getKey()+" -> "+ent.getValue()+" artifacts ("+commit_to_build_meta_url.get(ent.getKey())+")");
        }

        //Fetch release info of jcefmaven
        JSONArray jcefmaven = new GitHubAPIReleasesRequest((String) CONFIG.get("jcefMavenRepo")).performRequest(true);
        List<String> jcefmaven_commits = new LinkedList<>();
        for(Object object : jcefmaven) {
            JSONObject release = (JSONObject) object;
            String body = (String) release.get("body");
            String commit_id = body.substring(body.indexOf(jcefRepoCommitUrl) + jcefRepoCommitUrl.length());
            commit_id = commit_id.substring(0, commit_id.indexOf(")"));
            jcefmaven_commits.add(commit_id);
        }
        System.out.println();
        System.out.println("jcefmaven releases fetched:");
        System.out.println(Arrays.toString(jcefmaven_commits.toArray(new String[0])));
        System.out.println();

        //Issue missing jcefbuild builds
        //We only issue one build at a time to not create too much stress on GitHub infrastructure
        int index = -1;
        if(jcefbuild_commits.size()>0){
            //There have been builds already
            //Locate the last build in all commits and get the commit
            //that is one step newer
            index = commits.indexOf(jcefbuild_commits.get(0))-1;
        }else{
            //There have been no builds yet
            //Begin with the earliest version
            index = commits.size()-1;
        }
        //Trigger builds
        if(index>=0){
            System.out.println("JCEFBUILD> Triggering a build for "+commits.get(index));
            JSONObject inputs = new JSONObject();
            inputs.put("repo", CONFIG.get("jcefRepo"));
            inputs.put("ref", commits.get(index));
            new GitHubAPIDispatchWorkflowRequest((String) CONFIG.get("jcefBuildRepo"), (String) CONFIG.get("jcefBuildWorkflow"), inputs)
                    .performRequest(true);
            System.out.println("JCEFBUILD> Build triggered");
        }else{
            //Index is <0, which indicates that there is nothing to build
            System.out.println("JCEFBUILD> Builds are up to date");
        }

        //Issue missing jcefmaven builds
        //Only one build may be scheduled at a time, as GitHub Packages
        //marks the last upload as the "current version". When there is a
        //race condition in uploads, an older version may appear as current!
        //Anyways, we do not want multiple concurrent builds anyways
        index = -1;
        if(jcefmaven_commits.size()>0){
            //There have been builds already
            //Locate the last build in all commits and get the commit
            //that is one step newer
            index = jcefbuild_commits.indexOf(jcefmaven_commits.get(0))-1;
        }else{
            //There have been no builds yet
            //Begin with the earliest version
            index = jcefbuild_commits.size()-1;
        }
        //Trigger builds
        if(index>=0){
            String commit = jcefbuild_commits.get(index);
            Integer assets = commit_to_artifact_amount.get(commit);
            if(assets == null || assets < EXPECTED_ASSETS){
                System.out.println("JCEFMAVEN> Still waiting on jcefbuild build "+commit+" to complete");
            }else{
                String build_meta = commit_to_build_meta_url.get(commit);
                if(build_meta==null){
                    System.out.println("JCEFMAVEN> No build meta yet on jcefbuild build "+commit);
                }else {
                    System.out.println("JCEFMAVEN> Triggering a build for " + commit + " ("+build_meta+")");
                    JSONObject inputs = new JSONObject();
                    inputs.put("build_meta", build_meta);
                    new GitHubAPIDispatchWorkflowRequest((String) CONFIG.get("jcefMavenRepo"), (String) CONFIG.get("jcefMavenWorkflow"), inputs)
                            .performRequest(true);
                    System.out.println("JCEFMAVEN> Build triggered");
                }
            }
        }else{
            //Index is <0, which indicates that there is nothing to build
            System.out.println("JCEFMAVEN> Builds are up to date");
        }
    }
}
