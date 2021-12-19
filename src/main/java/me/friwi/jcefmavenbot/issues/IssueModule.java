package me.friwi.jcefmavenbot.issues;

import me.friwi.jcefmavenbot.JCefMavenBot;
import me.friwi.jcefmavenbot.github.api.GitHubAPIIssuesRequest;
import me.friwi.jcefmavenbot.github.api.GitHubAPIReleasesRequest;
import me.friwi.jcefmavenbot.github.api.GitHubIssue;
import me.friwi.jcefmavenbot.github.api.GitHubRelease;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class IssueModule {
    public static void processIssues() throws IOException {
        System.out.println("################################");
        System.out.println("#         Issue Module         #");
        System.out.println("################################\n");

        //Load data
        System.out.println("Fetching releases...");
        List<GitHubReleaseWithTestResults> releases = (List<GitHubReleaseWithTestResults>) new GitHubAPIReleasesRequest((String) JCefMavenBot.CONFIG.get("jcefMavenRepo"))
                .performRequest(true)
                .stream()
                .map(GitHubRelease::fromJSON)
                .map(GitHubReleaseWithTestResults::fromGitHubRelease)
                .collect(Collectors.toList());

        System.out.println("Fetching issues...");
        List<GitHubIssue> issues = (List<GitHubIssue>) new GitHubAPIIssuesRequest((String) JCefMavenBot.CONFIG.get("jcefMavenRepo"))
                .performRequest(true)
                .stream()
                .map(GitHubIssue::fromJSON)
                .filter(m->((GitHubIssue)m).getTitle().toUpperCase(Locale.ENGLISH).startsWith("[TR]")) //Only look at relevant [TR] elements
                .collect(Collectors.toList());


        //Test reports
        System.out.println("Working on test reports...");
        List<GitHubIssueWithTestReport> testReports = issues
                .stream()
                .filter(m-> m.getTitle().toUpperCase(Locale.ENGLISH).startsWith("[TR]")) //Only look at relevant [TR] elements
                .map(GitHubIssueWithTestReport::fromGitHubIssue)
                .collect(Collectors.toList());
        TestReportIssueWorker.processTestReports(releases, testReports);

        //Sync to GitHub
        System.out.println("Syncing to GitHub...");
        for(GitHubReleaseWithTestResults rel : releases){
            rel.update();
        }

        for(GitHubIssue issue : issues){
            issue.update();
        }
        System.out.println("Done.");
    }
}
