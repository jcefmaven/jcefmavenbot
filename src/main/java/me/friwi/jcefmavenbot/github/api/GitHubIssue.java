package me.friwi.jcefmavenbot.github.api;

import me.friwi.jcefmavenbot.github.GitHubAPIIssueLockReason;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GitHubIssue {
    private long number;
    private String title;
    private String body;
    private String user;

    private String htmlUrl;
    private String apiUrl;

    private List<String> assignees;
    private List<String> labels;
    private boolean open;
    private boolean locked;
    private GitHubAPIIssueLockReason activeLockReason;

    private GitHubIssue initialCopy = null;
    private List<String> addComments = new LinkedList<>();

    private List<GitHubIssueComment> comments = null;

    protected GitHubIssue(long number, String title, String body, String user, String htmlUrl, String apiUrl, List<String> assignees, List<String> labels, boolean open, boolean locked, GitHubAPIIssueLockReason activeLockReason) {
        this.number = number;
        this.title = title;
        this.body = body;
        this.user = user;
        this.htmlUrl = htmlUrl;
        this.apiUrl = apiUrl;
        this.assignees = assignees;
        this.labels = labels;
        this.open = open;
        this.locked = locked;
        this.activeLockReason = activeLockReason;
    }

    //Comment loading

    public List<GitHubIssueComment> getComments() throws IOException {
        if(comments==null){
            comments = List.copyOf((List<GitHubIssueComment>) new GitHubAPIIssueCommentsRequest(this)
                    .performRequest(true)
                    .stream()
                    .map(GitHubIssueComment::fromJSON)
                    .collect(Collectors.toList()));
        }
        return comments;
    }


    //Updating

    public void update() throws IOException {
        if(initialCopy==null)throw new RuntimeException("No original state was preserved!");
        //Add comments
        for(String comment : this.addComments){
            new GitHubAPICommentIssueRequest(this, comment).performRequest(true);
        }
        //Update issue
        if(!this.equals(initialCopy)){
            //Issue was modified
            if(this.isLocked() && !initialCopy.isLocked()){
                new GitHubAPILockIssueRequest(this, this.getActiveLockReason()).performRequest(true);
            }
            if(!this.isLocked() && initialCopy.isLocked()){
                throw new RuntimeException("Unlocking an issue is currently not supported!");
            }
            new GitHubAPIUpdateIssueRequest(this).performRequest(true);
        }
        initialCopy = copy();
    }

    //Parsing

    public static GitHubIssue fromJSON(Object o) {
        return fromJSON((JSONObject) o);
    }

    public static GitHubIssue fromJSON(JSONObject jsonObject){
        //Iterate assignees
        List<String> assignees = new LinkedList<>();
        for(Object assignee : (JSONArray)jsonObject.get("assignees")){
            assignees.add((String) ((JSONObject)assignee).get("login"));
        }
        assignees = Collections.unmodifiableList(assignees);
        //Iterate labels
        List<String> labels = new LinkedList<>();
        for(Object assignee : (JSONArray)jsonObject.get("labels")){
            labels.add((String) ((JSONObject)assignee).get("name"));
        }
        labels = Collections.unmodifiableList(labels);
        GitHubIssue ret = new GitHubIssue(
                (long) jsonObject.get("number"),
                (String) jsonObject.get("title"),
                (String) jsonObject.get("body"),
                (String) ((JSONObject)jsonObject.get("user")).get("login"),
                (String) jsonObject.get("html_url"),
                (String) jsonObject.get("url"),
                assignees,
                labels,
                jsonObject.get("state").equals("open"),
                (Boolean) jsonObject.get("locked"),
                GitHubAPIIssueLockReason.fromString((String) jsonObject.get("active_lock_reason"))
                );
        ret.initialCopy = ret.copy();
        return ret;
    }

    //Setters

    public void addComment(String comment){
        this.addComments.add(comment);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setAssignees(List<String> assignees) {
        this.assignees = List.copyOf(assignees);
    }

    public void setLabels(List<String> labels) {
        this.labels = List.copyOf(labels);
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void setActiveLockReason(GitHubAPIIssueLockReason activeLockReason) {
        this.activeLockReason = activeLockReason;
    }

    //Getters

    public long getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getUser() {
        return user;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public List<String> getAssignees() {
        return assignees;
    }

    public List<String> getLabels() {
        return labels;
    }

    public boolean isOpen() {
        return open;
    }

    public boolean isLocked() {
        return locked;
    }

    public GitHubAPIIssueLockReason getActiveLockReason() {
        return activeLockReason;
    }

    public GitHubIssue copy(){
        return new GitHubIssue(number, title, body, user, htmlUrl, apiUrl, assignees, labels, open, locked, activeLockReason);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GitHubIssue that = (GitHubIssue) o;
        return getNumber() == that.getNumber() && isOpen() == that.isOpen() && isLocked() == that.isLocked() && getTitle().equals(that.getTitle()) && getBody().equals(that.getBody()) && getUser().equals(that.getUser()) && getHtmlUrl().equals(that.getHtmlUrl()) && getApiUrl().equals(that.getApiUrl()) && getAssignees().equals(that.getAssignees()) && getLabels().equals(that.getLabels()) && Objects.equals(getActiveLockReason(), that.getActiveLockReason());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNumber(), getTitle(), getBody(), getUser(), getHtmlUrl(), getApiUrl(), getAssignees(), getLabels(), isOpen(), isLocked(), getActiveLockReason());
    }

    @Override
    public String toString() {
        return "GitHubIssue{" +
                "number='" + number + '\'' +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", user='" + user + '\'' +
                ", htmlUrl='" + htmlUrl + '\'' +
                ", apiUrl='" + apiUrl + '\'' +
                ", assignees=" + Arrays.toString(assignees.toArray(new String[0])) +
                ", labels=" + Arrays.toString(labels.toArray(new String[0])) +
                ", open=" + open +
                ", locked=" + locked +
                ", activeLockReason=" + activeLockReason +
                '}';
    }
}
