package me.friwi.jcefmavenbot.issues;

import me.friwi.jcefmavenbot.github.api.GitHubIssue;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class GitHubIssueWithTestReport {
    public static final String LINE_TAG = "**Tag";
    public static final String LINE_OS = "**OS";
    public static final String LINE_VERSION = "**Version";
    public static final String LINE_ARCH = "**Arch";
    public static final String LINE_WORKING = "**Report as working";
    public static final String LINE_SAMPLE_APP = "**Did you test with the official sample app";

    private GitHubIssue gitHubIssue;

    private EnumPlatform platform;
    private EnumTestResult testResult;
    private String mavenVersion;

    private List<String> err;

    protected GitHubIssueWithTestReport(GitHubIssue gitHubIssue, EnumPlatform platform, EnumTestResult testResult, String mavenVersion, List<String> err) {
        this.gitHubIssue = gitHubIssue;
        this.platform = platform;
        this.testResult = testResult;
        this.mavenVersion = mavenVersion;
        this.err = err;
    }

    public GitHubIssue getGitHubIssue() {
        return gitHubIssue;
    }

    public EnumPlatform getPlatform() {
        return platform;
    }

    public EnumTestResult getTestResult() {
        return testResult;
    }

    public String getMavenVersion() {
        return mavenVersion;
    }

    public List<String> getErr() {
        return err;
    }

    public static GitHubIssueWithTestReport fromGitHubIssue(Object issue){
        return fromGitHubIssue((GitHubIssue) issue);
    }

    public static GitHubIssueWithTestReport fromGitHubIssue(GitHubIssue issue){
        List<String> err = new LinkedList<>();
        String[] lines = issue.getBody().split("\n");

        String tag = getLineFromIssue(LINE_TAG, lines);
        String os = getLineFromIssue(LINE_OS, lines);
        String version = getLineFromIssue(LINE_VERSION, lines);
        String arch = getLineFromIssue(LINE_ARCH, lines);
        String working = getLineFromIssue(LINE_WORKING, lines);
        String sampleapp = getLineFromIssue(LINE_SAMPLE_APP, lines);

        //Verify tag
        if(tag==null)err.add("did not specify an artifact version");

        //Verify os
        if(os==null)err.add("did not specify an operating system");
        EnumOS os1 = EnumOS.fromString(os);
        if(os1==null&&os!=null)err.add("specified an invalid operating system");

        //Verify version/flavour
        if(version==null)err.add("did not specify an operating system version/flavour");

        //Verify arch
        if(arch==null)err.add("did not specify a system architecture");
        EnumArch arch1 = EnumArch.fromString(arch);
        if(arch1==null&&arch!=null)err.add("specified an invalid system architecture");

        //Verify working
        if(working==null)err.add("did not specify whether the artifact is working");
        Boolean working1 = parseYesNo(working);
        if(working1==null&&working!=null)err.add("specified an invalid value in the \"working?\" section");

        //Verify sampleapp
        if(sampleapp==null)err.add("did not specify whether you used the sample app");
        Boolean sampleapp1 = parseYesNo(sampleapp);
        if(sampleapp1==null&&sampleapp!=null)err.add("specified an invalid value in the sample app section");

        //Verify platform
        EnumPlatform platform = EnumPlatform.fromOSAndArch(os1, arch1);
        if(platform==null&&os1!=null&&arch1!=null)err.add("specified a combination of operating system and system architecture that we do not offer artifacts for");

        EnumTestResult testResult = (working1==null?null:(working1?EnumTestResult.WORKING:EnumTestResult.BROKEN));

        return new GitHubIssueWithTestReport(issue, platform, testResult, tag, err);
    }

    private static Boolean parseYesNo(String yn) {
        if(yn==null)return null;
        yn = yn.toLowerCase(Locale.ENGLISH);
        if(yn.equals("y")||yn.equals("yes"))return true;
        if(yn.equals("n")||yn.equals("no"))return false;
        return null;
    }

    private static String getLineFromIssue(String previousLineStart, String[] lines){
        int index = -1;
        for(int i = 0; i < lines.length; i++){
            if(lines[i].toLowerCase(Locale.ENGLISH).startsWith(previousLineStart.toLowerCase(Locale.ENGLISH))){
                index = i;
                break;
            }
        }
        index++; //We want the next line
        if(index > 0 && index < lines.length){
            return lines[index];
        }
        return null; //Not found
    }
}
