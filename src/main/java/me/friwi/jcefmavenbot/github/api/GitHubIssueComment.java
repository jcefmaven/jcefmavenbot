package me.friwi.jcefmavenbot.github.api;

import org.json.simple.JSONObject;

public class GitHubIssueComment {
    private String user;
    private String body;
    private String apiUrl;
    private String authorAssociation;

    private GitHubIssueComment(String user, String body, String apiUrl, String authorAssociation) {
        this.user = user;
        this.body = body;
        this.apiUrl = apiUrl;
        this.authorAssociation = authorAssociation;
    }

    public String getUser() {
        return user;
    }

    public String getBody() {
        return body;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getAuthorAssociation() {
        return authorAssociation;
    }

    public static GitHubIssueComment fromJSON(Object o){
        return fromJSON((JSONObject) o);
    }

    public static GitHubIssueComment fromJSON(JSONObject o){
        return new GitHubIssueComment(
                (String) ((JSONObject)o.get("user")).get("login"),
                (String) o.get("body"),
                (String) o.get("url"),
                (String) o.get("author_association")
        );
    }
}
