package me.friwi.jcefmavenbot.buildissuer;

import me.friwi.jcefmavenbot.JCefMavenBot;
import me.friwi.jcefmavenbot.github.api.*;
import me.friwi.jcefmavenbot.qualitycontrol.QualityControl;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static me.friwi.jcefmavenbot.JCefMavenBot.CONFIG;

public class BuildIssuer {
    public static final boolean PERFORM_ACTIONS = true;
    public static final int EXPECTED_ASSETS = 13;

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
        Map<String, String> commit_to_cef_version = new HashMap<>();
        for(Object object : jcefbuilds){
            JSONObject release = (JSONObject) object;
            String body = (String) release.get("body");
            String commit_id = body.substring(body.indexOf(jcefRepoCommitUrl)+jcefRepoCommitUrl.length());
            commit_id = commit_id.substring(0, commit_id.indexOf(")"));
            jcefbuild_commits.add(commit_id);
            JSONArray assets = ((JSONArray) release.get("assets"));
            commit_to_artifact_amount.put(commit_id, assets.size());
            if(assets.size()>=EXPECTED_ASSETS-1) {
                for (Object a : assets) {
                    JSONObject asset = (JSONObject) a;
                    if(asset.get("name").equals("build_meta.json")){
                        commit_to_build_meta_url.put(commit_id, (String) asset.get("browser_download_url"));
                        break;
                    }
                }
            }
            int inds = body.indexOf("CEF version: ");
            if(inds!=-1){
                String bodysub = body.substring(inds+13);
                inds = bodysub.indexOf("CEF version: "); //JCEF version: also contains CEF version ;)
                bodysub = bodysub.substring(inds+13);
                if(inds!=-1){
                    int inde = bodysub.indexOf("\n");
                    if(inde!=-1) {
                        commit_to_cef_version.put(commit_id, bodysub.substring(0, inde).trim());
                    }
                }
            }
        }
        System.out.println();
        System.out.println("jcefbuild releases fetched:");
        System.out.println(Arrays.toString(jcefbuild_commits.toArray(new String[0])));
        for(String commit : jcefbuild_commits){
            System.out.println(commit+" -> "+commit_to_artifact_amount.get(commit)+" artifacts ("+commit_to_build_meta_url.get(commit)+")");
        }

        //Fetch release info of jcefmaven
        JSONArray jcefmaven = new GitHubAPIReleasesRequest((String) CONFIG.get("jcefMavenRepo")).performRequest(true);
        List<String> jcefmaven_commits = new LinkedList<>();
        Map<String, String> commit_to_jcefmaven_version = new HashMap<>();
        for(Object object : jcefmaven) {
            JSONObject release = (JSONObject) object;
            String body = (String) release.get("body");
            String commit_id = body.substring(body.indexOf(jcefRepoCommitUrl) + jcefRepoCommitUrl.length());
            commit_id = commit_id.substring(0, commit_id.indexOf(")"));
            jcefmaven_commits.add(commit_id);
            int inds = body.indexOf("<version>");
            int inde = body.indexOf("</version>");
            if(inds!=-1 && inde!=-1){
                commit_to_jcefmaven_version.put(commit_id, body.substring(inds+9, inde).trim());
            }
        }
        System.out.println();
        System.out.println("jcefmaven releases fetched:");
        System.out.println(Arrays.toString(jcefmaven_commits.toArray(new String[0])));
        System.out.println();

        //Fetch release info of jcefsampleapp
        JSONArray jcefsampleapp = new GitHubAPIReleasesRequest((String) CONFIG.get("jcefSampleAppRepo")).performRequest(true);
        List<String> jcefsampleapp_commits = new LinkedList<>();
        for(Object object : jcefsampleapp) {
            JSONObject release = (JSONObject) object;
            String body = (String) release.get("body");
            String commit_id = body.substring(body.indexOf(jcefRepoCommitUrl) + jcefRepoCommitUrl.length());
            commit_id = commit_id.substring(0, commit_id.indexOf(")"));
            jcefsampleapp_commits.add(commit_id);
        }
        System.out.println();
        System.out.println("jcefsampleapp releases fetched:");
        System.out.println(Arrays.toString(jcefsampleapp_commits.toArray(new String[0])));
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
        //Trigger builds for jcefbuild
        if(index>=0){
            System.out.println("JCEFBUILD> Triggering a build for "+commits.get(index));
            JSONObject inputs = new JSONObject();
            inputs.put("repo", CONFIG.get("jcefRepo"));
            inputs.put("ref", commits.get(index));
            if(PERFORM_ACTIONS){
                new GitHubAPIDispatchWorkflowRequest((String) CONFIG.get("jcefBuildRepo"), (String) CONFIG.get("jcefBuildWorkflow"), inputs)
                        .performRequest(true);
            }
            System.out.println("JCEFBUILD> Build triggered");
        }else{
            //Index is <0, which indicates that there is nothing to build
            System.out.println("JCEFBUILD> Builds are up to date");
        }

        //Perform testing on jcefbuild and exclude invalid builds
        List<String> commitsToRemove = new LinkedList<>();
        for(Object b : jcefbuilds){
            JSONObject release = (JSONObject) b;
            String body = (String) release.get("body");
            String commit_id = body.substring(body.indexOf(jcefRepoCommitUrl) + jcefRepoCommitUrl.length());
            commit_id = commit_id.substring(0, commit_id.indexOf(")"));
            if(commit_to_artifact_amount.get(commit_id)==EXPECTED_ASSETS-1){
                System.out.println("QUALITY_CONTR> Checking artifacts for "+commit_id);
                GitHubRelease r = GitHubRelease.fromJSON(release);
                //Test!
                if(PERFORM_ACTIONS) {
                    List[] report = QualityControl.generateTestReport(commit_to_build_meta_url.get(commit_id));
                    if (!QualityControl.isOK(report)) {
                        //Open issue
                        String short_commit = commit_id.substring(0, 7);
                        String title = "[BUG] Build for " + short_commit + " failed";
                        String content = "@" + CONFIG.get("maintainerUser") + " The [build for " + short_commit + "](" + r.getHtmlUrl() + ") just failed. Please check.";
                        JSONArray labels = new JSONArray();
                        labels.add("bug");
                        JSONArray assignees = new JSONArray();
                        assignees.add(CONFIG.get("maintainerUser"));
                        JSONObject obj = (JSONObject) new GitHubAPICreateIssueRequest((String) CONFIG.get("jcefBuildRepo"), title, content, labels, assignees)
                                .performRequest(true);
                        String issue = obj.get("number").toString();
                        String issueUrl = obj.get("html_url").toString();
                        //Upload new body
                        body = QualityControl.generateWarning(report, issue, issueUrl) + body;
                        r.setBody(body);
                        r.update();
                        System.out.println("QUALITY_CONTR> Marked artifact as not successful!");
                        commitsToRemove.add(commit_id);
                    }
                    //Upload
                    JSONObject testResult = QualityControl.formatTestReport(report);
                    r.uploadArtifact("test_report.json", testResult.toString().getBytes(StandardCharsets.UTF_8));
                    commit_to_artifact_amount.put(commit_id, EXPECTED_ASSETS);
                }
                System.out.println("QUALITY_CONTR> Uploaded artifact!");
            }else{
                if(body.contains(">**WARNING"))commitsToRemove.add(commit_id);
            }
        }
        //Ignore broken commits for maven builds
        for(String c : commitsToRemove){
            jcefbuild_commits.remove(c);
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
        //Trigger builds for jcefmaven
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
                    //Check that last jcefmaven build is already synced
                    if(jcefmaven_commits.size()>0 && !MavenCentralSyncChecker.checkAllSynced(
                            commit_to_jcefmaven_version.get(jcefmaven_commits.get(0)),
                            "jcef-"+jcefmaven_commits.get(0).substring(0, 7)+"+cef-"+commit_to_cef_version.get(jcefmaven_commits.get(0))
                    )){
                        System.out.println("JCEFMAVEN> Delaying build, as the previous one is not synced yet");
                    }else{
                        SemanticVersion previousMavenVersion = SemanticVersion.fromString(jcefmaven_commits.size()>0?
                                commit_to_jcefmaven_version.get(jcefmaven_commits.get(0)):
                                "0.0.0");
                        String cefver = commit_to_cef_version.get(commit);
                        if(cefver==null){
                            System.out.println("JCEFMAVEN> No cef version yet for "+commit);
                        }else {
                            cefver = cefver.substring(0, cefver.indexOf("+"));
                            SemanticVersion currentCefVersion = SemanticVersion.fromString(cefver);
                            String mvn_version;
                            if (previousMavenVersion.isSamePatch(currentCefVersion)) {
                                //Increase previous maven version by one prerelease
                                previousMavenVersion.increasePrerelease();
                                mvn_version = previousMavenVersion.toString();
                            } else {
                                //Else use new cef version
                                mvn_version = currentCefVersion.toString();
                            }
                            System.out.println("JCEFMAVEN> Triggering a build for " + commit + " (" + mvn_version + ", " + build_meta + ")");
                            JSONObject inputs = new JSONObject();
                            inputs.put("build_meta", build_meta);
                            inputs.put("mvn_version", mvn_version);
                            if(PERFORM_ACTIONS) {
                                new GitHubAPIDispatchWorkflowRequest((String) CONFIG.get("jcefMavenRepo"), (String) CONFIG.get("jcefMavenWorkflow"), inputs)
                                        .performRequest(true);
                            }
                            System.out.println("JCEFMAVEN> Build triggered");
                        }
                    }
                }
            }
        }else{
            //Index is <0, which indicates that there is nothing to build
            System.out.println("JCEFMAVEN> Builds are up to date");
        }



        //Issue missing jcefsampleapp builds
        //Only issue builds for versions that are already fully synced!
        index = -1;
        if(jcefsampleapp_commits.size()>0){
            //There have been builds already
            //Locate the last build in all commits and get the commit
            //that is one step newer
            index = jcefmaven_commits.indexOf(jcefsampleapp_commits.get(0))-1;
        }else{
            //There have been no builds yet
            //Begin with the earliest version
            index = jcefmaven_commits.size()-1;
        }
        //Trigger builds for jcefsampleapp
        if(index>=0){
            String commit = jcefmaven_commits.get(index);
            String mvn_version = commit_to_jcefmaven_version.get(commit);
            String cef_version = "jcef-"+commit.substring(0, 7)+"+cef-"+commit_to_cef_version.get(commit);
            if(mvn_version==null){
                System.out.println("JCEFSAMPLEAPP> No maven version yet on jcefmaven build " + commit);
            } else {
                if (!MavenCentralSyncChecker.checkAllSynced(mvn_version, cef_version)) {
                    System.out.println("JCEFSAMPLEAPP> Still waiting on central sync for " + mvn_version);
                } else {
                    String build_meta = commit_to_build_meta_url.get(commit);
                    if (build_meta == null) {
                        System.out.println("JCEFSAMPLEAPP> No build meta yet on jcefbuild build " + commit);
                    } else {
                        System.out.println("JCEFSAMPLEAPP> Triggering a build for " + commit + " (" + mvn_version + " | " + build_meta + ")");
                        JSONObject inputs = new JSONObject();
                        inputs.put("build_meta", build_meta);
                        inputs.put("mvn_version", mvn_version);
                        if(PERFORM_ACTIONS){
                            new GitHubAPIDispatchWorkflowRequest((String) CONFIG.get("jcefSampleAppRepo"), (String) CONFIG.get("jcefSampleAppWorkflow"), inputs)
                                 .performRequest(true);
                        }
                        System.out.println("JCEFSAMPLEAPP> Build triggered");
                    }
                }
            }
        }else{
            //Index is <0, which indicates that there is nothing to build
            System.out.println("JCEFSAMPLE> Builds are up to date");
        }
    }
}
