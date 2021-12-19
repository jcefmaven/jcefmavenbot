package me.friwi.jcefmavenbot.github.api;

import me.friwi.jcefmavenbot.github.GitHubAPIRequest;
import org.json.simple.JSONArray;

import static me.friwi.jcefmavenbot.github.GitHubAPIRequestMethod.PATCH;

public class GitHubAPIUpdateReleaseRequest extends GitHubAPIRequest {
    public GitHubAPIUpdateReleaseRequest(GitHubRelease release) {
        super(PATCH, release.getApiUrl());
        this.addAttribute("tag_name", release.getTagName());
        this.addAttribute("name", release.getName());
        this.addAttribute("body", release.getBody());
    }
}
