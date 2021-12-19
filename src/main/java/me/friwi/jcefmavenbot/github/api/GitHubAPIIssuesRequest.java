package me.friwi.jcefmavenbot.github.api;

import me.friwi.jcefmavenbot.github.GitHubAPIPaginatedListRequest;

import static me.friwi.jcefmavenbot.github.GitHubAPIRequestMethod.GET;

public class GitHubAPIIssuesRequest extends GitHubAPIPaginatedListRequest {
    public GitHubAPIIssuesRequest(String ownerAndRepo) {
        super(GET, "/repos/" + ownerAndRepo + "/issues");
    }
}
