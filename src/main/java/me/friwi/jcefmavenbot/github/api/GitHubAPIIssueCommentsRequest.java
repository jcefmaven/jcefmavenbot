package me.friwi.jcefmavenbot.github.api;

import me.friwi.jcefmavenbot.github.GitHubAPIPaginatedListRequest;

import static me.friwi.jcefmavenbot.github.GitHubAPIRequestMethod.GET;

public class GitHubAPIIssueCommentsRequest extends GitHubAPIPaginatedListRequest {
    public GitHubAPIIssueCommentsRequest(GitHubIssue issue) {
        super(GET, issue.getApiUrl()+"/comments");
    }
}
