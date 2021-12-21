package me.friwi.jcefmavenbot.github.api;

import me.friwi.jcefmavenbot.github.GitHubAPIRequest;
import org.json.simple.JSONArray;

import java.util.Collection;

import static me.friwi.jcefmavenbot.github.GitHubAPIRequestMethod.POST;

public class GitHubAPICreateIssueRequest extends GitHubAPIRequest {
    public GitHubAPICreateIssueRequest(String ownerAndRepo, String title, String body, JSONArray labels, JSONArray assignees) {
        super(POST, "/repos/"+ownerAndRepo+"/issues");
        this.addAttribute("title", title);
        this.addAttribute("body", body);
        this.addAttribute("labels", labels);
        this.addAttribute("assignees", assignees);
    }
}
