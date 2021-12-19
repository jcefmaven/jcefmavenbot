package me.friwi.jcefmavenbot.github.api;

import me.friwi.jcefmavenbot.github.GitHubAPIRequest;
import org.json.simple.JSONArray;

import static me.friwi.jcefmavenbot.github.GitHubAPIRequestMethod.PATCH;
import static me.friwi.jcefmavenbot.github.GitHubAPIRequestMethod.POST;

public class GitHubAPICommentIssueRequest extends GitHubAPIRequest {
    public GitHubAPICommentIssueRequest(GitHubIssue issue, String comment) {
        super(POST, issue.getApiUrl()+"/comments");
        this.addAttribute("body", comment);
    }
}
