package me.friwi.jcefmavenbot.github.api;

import me.friwi.jcefmavenbot.github.GitHubAPIIssueLockReason;
import me.friwi.jcefmavenbot.github.GitHubAPIRequest;

import static me.friwi.jcefmavenbot.github.GitHubAPIRequestMethod.POST;
import static me.friwi.jcefmavenbot.github.GitHubAPIRequestMethod.PUT;

public class GitHubAPILockIssueRequest extends GitHubAPIRequest {
    public GitHubAPILockIssueRequest(GitHubIssue issue, GitHubAPIIssueLockReason reason) {
        super(PUT, issue.getApiUrl()+"/lock");
        this.addAttribute("lock_reason", reason.getValue());
    }
}
