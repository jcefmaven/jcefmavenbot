package me.friwi.jcefmavenbot.github.api;

import me.friwi.jcefmavenbot.github.GitHubAPIRequest;
import me.friwi.jcefmavenbot.github.GitHubAPIRequestMethod;
import org.json.simple.JSONObject;

import static me.friwi.jcefmavenbot.github.GitHubAPIRequestMethod.POST;

public class GitHubAPIDispatchWorkflowRequest extends GitHubAPIRequest {
    public GitHubAPIDispatchWorkflowRequest(String ownerAndRepo, String workflow, JSONObject inputs) {
        super(POST, "/repos/"+ownerAndRepo+"/actions/workflows/"+workflow+"/dispatches");
        this.addAttribute("ref", "master");
        this.addAttribute("inputs", inputs);
    }
}
