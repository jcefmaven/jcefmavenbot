package me.friwi.jcefmavenbot.github.api;

import me.friwi.jcefmavenbot.github.GitHubAPIRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import static me.friwi.jcefmavenbot.github.GitHubAPIRequestMethod.PATCH;
import static me.friwi.jcefmavenbot.github.GitHubAPIRequestMethod.POST;

public class GitHubAPIUpdateIssueRequest extends GitHubAPIRequest {
    public GitHubAPIUpdateIssueRequest(GitHubIssue issue) {
        super(PATCH, issue.getApiUrl());
        this.addAttribute("title", issue.getTitle());
        this.addAttribute("body", issue.getBody());
        this.addAttribute("state", issue.isOpen()?"open":"closed");
        JSONArray labels = new JSONArray();
        for(String s : issue.getLabels())labels.add(s);
        JSONArray assignees = new JSONArray();
        for(String s : issue.getAssignees())assignees.add(s);
        this.addAttribute("labels", labels);
        this.addAttribute("assignees", assignees);
    }
}
