package me.friwi.jcefmavenbot.github.api;

import me.friwi.jcefmavenbot.github.GitHubAPIPaginatedListRequest;

import static me.friwi.jcefmavenbot.github.GitHubAPIRequestMethod.GET;

public class GitHubAPIReleasesRequest extends GitHubAPIPaginatedListRequest {
    public GitHubAPIReleasesRequest(String ownerAndRepo) {
        super(GET, "/repos/" + ownerAndRepo + "/releases");
    }
}
