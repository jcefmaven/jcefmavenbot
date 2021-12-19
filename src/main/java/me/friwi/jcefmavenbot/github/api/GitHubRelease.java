package me.friwi.jcefmavenbot.github.api;

import me.friwi.jcefmavenbot.github.GitHubAPIIssueLockReason;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.*;

public class GitHubRelease {
    private String tagName;
    private String name;
    private String body;

    private String htmlUrl;
    private String apiUrl;

    private GitHubRelease initialCopy = null;

    protected GitHubRelease(String tagName, String name, String body, String htmlUrl, String apiUrl) {
        this.tagName = tagName;
        this.name = name;
        this.body = body;
        this.htmlUrl = htmlUrl;
        this.apiUrl = apiUrl;
    }

    //Updating

    public void update() throws IOException {
        if(initialCopy==null)throw new RuntimeException("No original state was preserved!");
        //Update release
        if(!this.equals(initialCopy)){
            new GitHubAPIUpdateReleaseRequest(this).performRequest(true);
        }
        initialCopy = copy();
    }

    //Parsing

    public static GitHubRelease fromJSON(Object o) {
        return fromJSON((JSONObject) o);
    }

    public static GitHubRelease fromJSON(JSONObject jsonObject){
        GitHubRelease ret = new GitHubRelease(
                (String) jsonObject.get("tag_name"),
                (String) jsonObject.get("name"),
                (String) jsonObject.get("body"),
                (String) jsonObject.get("html_url"),
                (String) jsonObject.get("url")
                );
        ret.initialCopy = ret.copy();
        return ret;
    }

    //Setters

    public void setName(String name) {
        this.name = name;
    }

    public void setBody(String body) {
        this.body = body;
    }


    //Getters


    public String getTagName() {
        return tagName;
    }

    public String getName() {
        return name;
    }

    public String getBody() {
        return body;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public GitHubRelease copy(){
        return new GitHubRelease(tagName, name, body, htmlUrl, apiUrl);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GitHubRelease that = (GitHubRelease) o;
        return getTagName().equals(that.getTagName()) && getName().equals(that.getName()) && getBody().equals(that.getBody()) && getHtmlUrl().equals(that.getHtmlUrl()) && getApiUrl().equals(that.getApiUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTagName(), getName(), getBody(), getHtmlUrl(), getApiUrl());
    }

    @Override
    public String toString() {
        return "GitHubRelease{" +
                "tagName='" + tagName + '\'' +
                ", name='" + name + '\'' +
                ", body='" + body + '\'' +
                ", htmlUrl='" + htmlUrl + '\'' +
                ", apiUrl='" + apiUrl + '\'' +
                '}';
    }
}
