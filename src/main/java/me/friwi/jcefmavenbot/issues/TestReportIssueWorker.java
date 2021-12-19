package me.friwi.jcefmavenbot.issues;

import me.friwi.jcefmavenbot.JCefMavenBot;
import me.friwi.jcefmavenbot.github.GitHubAPIIssueLockReason;
import me.friwi.jcefmavenbot.github.api.GitHubIssueComment;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class TestReportIssueWorker {
    public static String BOT_USER, MAINTAINER_USER;

    public static final String LABEL_INVALID = "invalid";
    public static final String LABEL_DUPLICATE = "duplicate";
    public static final String LABEL_TEST_REPORT = "test report";

    public static final String LABEL_WORKING = "working";
    public static final String LABEL_CONFLICT = "conflict";
    public static final String LABEL_BROKEN = "broken";

    public static void processTestReports(List<GitHubReleaseWithTestResults> releases, List<GitHubIssueWithTestReport> issues) throws IOException {
        if(BOT_USER==null)BOT_USER = (String) JCefMavenBot.CONFIG.get("botUser");
        if(MAINTAINER_USER==null)MAINTAINER_USER = (String) JCefMavenBot.CONFIG.get("maintainerUser");
        for(GitHubIssueWithTestReport issue : issues){
            processTestReport(releases, issue);
        }
    }

    public static void processTestReport(List<GitHubReleaseWithTestResults> releases, GitHubIssueWithTestReport issue) throws IOException {
        //Locate release
        GitHubReleaseWithTestResults release = null;
        if(issue.getMavenVersion()!=null) {
            for (GitHubReleaseWithTestResults r : releases) {
                if (r.getMavenVersion().equals(issue.getMavenVersion())){
                    release = r;
                    break;
                }
            }
            if(release==null)issue.getErr().add("specified an invalid maven artifact version/tag");
        }

        //Check for errors
        if(issue.getErr().size()>0){
            StringBuilder comment = new StringBuilder("@" + issue.getGitHubIssue().getUser() + " Thank you for your test report! Sadly, your report " +
                    "could not be processed because you ");
            int i = 0;
            int l = issue.getErr().size();
            for(String e : issue.getErr()){
                comment.append(e);
                if(i==l-2){
                    comment.append(" and ");
                }else if(i!=l-1){
                    comment.append(", ");
                }
                i++;
            }
            comment.append(". Please open another issue and report your findings again!");
            issue.getGitHubIssue().addComment(comment.toString());
            issue.getGitHubIssue().setAssignees(Collections.singletonList(BOT_USER));
            issue.getGitHubIssue().setLabels(List.of(LABEL_INVALID));
            issue.getGitHubIssue().setOpen(false);
            issue.getGitHubIssue().setLocked(true);
            issue.getGitHubIssue().setActiveLockReason(GitHubAPIIssueLockReason.RESOLVED);
            return;
        }

        //Apply title to all issues
        applyTitle(issue);

        //Check if we need to run custom conflict/broken logic and not really process the issue from the beginning
        if(issue.getGitHubIssue().getLabels().contains(LABEL_BROKEN) || issue.getGitHubIssue().getLabels().contains(LABEL_CONFLICT)){
            //Check for resolving comment
            GitHubIssueComment relevantComment = null;
            for(GitHubIssueComment comment : issue.getGitHubIssue().getComments()){
                if(comment.getUser().equals(BOT_USER)){
                    //We replied - all previous comments are not relevant anymore
                    relevantComment = null;
                }else if(comment.getAuthorAssociation().equals("COLLABORATOR")
                || comment.getAuthorAssociation().equals("CONTRIBUTOR")
                || comment.getAuthorAssociation().equals("MEMBER")
                || comment.getAuthorAssociation().equals("OWNER")){
                    if(comment.getBody().contains("@"+BOT_USER))relevantComment = comment;
                }
            }
            //Check if we have a comment to process
            if(relevantComment!=null){
                String[] strings = relevantComment.getBody().toUpperCase(Locale.ENGLISH).split("\n");
                for(String s : strings){
                    String[] args = s.split(" ");
                    if(args.length>=3){
                        if(args[0].equals("@"+BOT_USER.toUpperCase(Locale.ENGLISH))
                            && (args[1].equals("MARK") || args[1].equals("RESOLVE"))){
                            EnumTestResult result = EnumTestResult.fromString(args[2]);
                            if(result==null || result==EnumTestResult.UNTESTED || result == EnumTestResult.CONFLICT){
                                //Reply with error
                                issue.getGitHubIssue().addComment("@"+relevantComment.getUser()+" Could not resolve the issue, as the specified result is invalid.");
                            }else{
                                //Update release
                                release.getTestResult(issue.getPlatform()).setTestResult(result);
                                release.getTestResult(issue.getPlatform()).setHref(issue.getGitHubIssue().getHtmlUrl());

                                //Update issue
                                String comment = "@"+relevantComment.getUser()+" Thank you for resolving this issue. The artifact is now marked as "
                                        +result.name()+".";
                                issue.getGitHubIssue().addComment(comment);
                                issue.getGitHubIssue().setAssignees(Collections.singletonList(BOT_USER));
                                List<String> labels = new LinkedList<>();
                                labels.add(LABEL_TEST_REPORT);
                                if(result==EnumTestResult.WORKING)labels.add(LABEL_WORKING);
                                if(result==EnumTestResult.BROKEN)labels.add(LABEL_BROKEN);
                                labels.add(getPlatformLabel(issue.getPlatform()));
                                issue.getGitHubIssue().setLabels(labels);
                                issue.getGitHubIssue().setOpen(false);
                                issue.getGitHubIssue().setLocked(true);
                                issue.getGitHubIssue().setActiveLockReason(GitHubAPIIssueLockReason.RESOLVED);
                            }
                        }
                    }
                }
            }
            return;
        }

        EnumTestResult previous = release.getTestResult(issue.getPlatform()).getTestResult();
        EnumTestResult now = issue.getTestResult();

        //Report is valid, lets check for a conflict with the release
        if((previous == EnumTestResult.WORKING && now == EnumTestResult.BROKEN) ||
                (previous == EnumTestResult.BROKEN && now == EnumTestResult.WORKING)){
            String prevIssue = getIssueNumberFromUrl(release.getTestResult(issue.getPlatform()).getHref());

            //Update release
            release.getTestResult(issue.getPlatform()).setTestResult(EnumTestResult.CONFLICT);
            release.getTestResult(issue.getPlatform()).setHref(issue.getGitHubIssue().getHtmlUrl());

            //Create a reply with maintainer mention and other issue
            String comment = "@"+issue.getGitHubIssue().getUser()+" Thank you for your test report! " +
                    "You specified a contradicting result compared to #"+prevIssue+", so some manual checking will be required.\n" +
                    "\n@"+MAINTAINER_USER+" Conflicting test reports. Report resolve with `@"+BOT_USER+" resolve <working/broken>` on a separate line.";
            issue.getGitHubIssue().addComment(comment);
            issue.getGitHubIssue().setAssignees(Collections.singletonList(MAINTAINER_USER));
            issue.getGitHubIssue().setLabels(List.of(LABEL_TEST_REPORT, LABEL_CONFLICT, getPlatformLabel(issue.getPlatform())));
            return;
        }

        //Check if previously we were in a conflict
        if(previous==EnumTestResult.CONFLICT){
            //This means we can reference the other issue here and close
            String prevIssue = getIssueNumberFromUrl(release.getTestResult(issue.getPlatform()).getHref());

            String comment = "@"+issue.getGitHubIssue().getUser()+" Thank you for your test report! We are currently " +
                    "investigating contradicting test reports for this artifact in #"+prevIssue+". Until resolving there, " +
                    "the artifact will remain marked as a conflict.";
            issue.getGitHubIssue().addComment(comment);
            issue.getGitHubIssue().setAssignees(Collections.singletonList(BOT_USER));
            issue.getGitHubIssue().setLabels(List.of(LABEL_TEST_REPORT, LABEL_DUPLICATE, getPlatformLabel(issue.getPlatform())));
            issue.getGitHubIssue().setOpen(false);
            issue.getGitHubIssue().setLocked(true);
            issue.getGitHubIssue().setActiveLockReason(GitHubAPIIssueLockReason.RESOLVED);
            return;
        }

        //Check for equal reports
        if((previous == EnumTestResult.WORKING && now == EnumTestResult.WORKING) ||
                (previous == EnumTestResult.BROKEN && now == EnumTestResult.BROKEN)){
            //Another user already reported. Mention other issue and close
            String prevIssue = getIssueNumberFromUrl(release.getTestResult(issue.getPlatform()).getHref());

            String comment = "@"+issue.getGitHubIssue().getUser()+" Thank you for your test report! Another user " +
                    "already reported the same result for this artifact in #"+prevIssue+". This issue will be marked as" +
                    "duplicate.";
            issue.getGitHubIssue().addComment(comment);
            issue.getGitHubIssue().setAssignees(Collections.singletonList(BOT_USER));
            issue.getGitHubIssue().setLabels(List.of(LABEL_TEST_REPORT, LABEL_DUPLICATE, getPlatformLabel(issue.getPlatform())));
            issue.getGitHubIssue().setOpen(false);
            issue.getGitHubIssue().setLocked(true);
            issue.getGitHubIssue().setActiveLockReason(GitHubAPIIssueLockReason.RESOLVED);
            return;
        }

        //First BROKEN report?
        if(now==EnumTestResult.BROKEN){
            //Update release
            release.getTestResult(issue.getPlatform()).setTestResult(EnumTestResult.BROKEN);
            release.getTestResult(issue.getPlatform()).setHref(issue.getGitHubIssue().getHtmlUrl());

            //Update issue
            String comment = "@"+issue.getGitHubIssue().getUser()+" Thank you for your test report! We will try to look into " +
                    "the reason why the build is not working. We would appreciate any help from you that we can get to have this issue resolved.\n\n" +
                    "@"+MAINTAINER_USER+" Broken build reported. Report resolve with `@\"+BOT_USER+\" resolve <working/broken>` on a separate line.";
            issue.getGitHubIssue().addComment(comment);
            issue.getGitHubIssue().setAssignees(Collections.singletonList(MAINTAINER_USER));
            issue.getGitHubIssue().setLabels(List.of(LABEL_TEST_REPORT, LABEL_BROKEN, getPlatformLabel(issue.getPlatform())));
            return;
        }

        //Mark as working
        //Update release
        release.getTestResult(issue.getPlatform()).setTestResult(EnumTestResult.WORKING);
        release.getTestResult(issue.getPlatform()).setHref(issue.getGitHubIssue().getHtmlUrl());

        //Update issue
        String comment = "@"+issue.getGitHubIssue().getUser()+" Thank you for your test report! We will mark the " +
                "artifact as tested and working.";
        issue.getGitHubIssue().addComment(comment);
        issue.getGitHubIssue().setAssignees(Collections.singletonList(BOT_USER));
        issue.getGitHubIssue().setLabels(List.of(LABEL_TEST_REPORT, LABEL_WORKING, getPlatformLabel(issue.getPlatform())));
        issue.getGitHubIssue().setOpen(false);
        issue.getGitHubIssue().setLocked(true);
        issue.getGitHubIssue().setActiveLockReason(GitHubAPIIssueLockReason.RESOLVED);
    }

    private static String getPlatformLabel(EnumPlatform platform){
        return platform.getOS().getValue()+"-"+platform.getArch().getValue();
    }

    private static void applyTitle(GitHubIssueWithTestReport issue){
        issue.getGitHubIssue().setTitle("[TR] For "+issue.getMavenVersion());
    }

    private static String getIssueNumberFromUrl(String url){
        String[] s = url.split("/");
        return s[s.length-1];
    }
}
