package me.friwi.jcefmavenbot.github;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class GitHubAPIPaginatedListRequest extends GitHubAPIRequest<JSONArray> {
    public GitHubAPIPaginatedListRequest(GitHubAPIRequestMethod method, String endpoint) {
        super(method, endpoint);
    }

    public GitHubAPIPaginatedListRequest(GitHubAPIRequestMethod method, String endpoint, JSONObject attributes) {
        super(method, endpoint, attributes);
    }

    @Override
    public JSONArray performRequest(boolean authorize) throws IOException{
        int page = 1;
        JSONArray result = new JSONArray();
        while(true){
            super.addAttribute("page", page);
            try{
                JSONArray contents = super.performRequest(authorize);
                if(contents.size()==0){
                    //We have reached the end
                    return result;
                }
                result.addAll(contents);
            }catch (IOException e){
                if(page==1){
                    //Error on page 1 means a real error
                    throw new IOException("Error while fetching array from GitHub API", e);
                }else{
                    //We have reached the end
                    return result;
                }
            }
            page++;
        }
    }
}
