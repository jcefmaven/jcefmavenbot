package me.friwi.jcefmavenbot.github;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GitHubAPIRequest<T extends Object> {
    public static final String API_SERVER = "https://api.github.com";
    private static final JSONParser PARSER = new JSONParser();

    private GitHubAPIRequestMethod method;
    private String endpoint;
    private JSONObject attributes;

    public GitHubAPIRequest(GitHubAPIRequestMethod method, String endpoint){
        this(method, endpoint, new JSONObject());
    }

    public GitHubAPIRequest(GitHubAPIRequestMethod method, String endpoint, JSONObject attributes){
        this.method = method;
        this.endpoint = endpoint;
        this.attributes = attributes;
    }

    public void addAttribute(String key, Object value){
        this.attributes.put(key, value);
    }

    public T performRequest(boolean authorize) throws IOException{
        try {
            HttpURLConnection connection;
            if(method==GitHubAPIRequestMethod.GET){
                String params = "";
                for(Map.Entry<Object, Object> entry : (Set<Map.Entry<Object, Object>>)attributes.entrySet()){
                    params += (params.isEmpty()?"?":"&")+entry.getKey()+"="+entry.getValue();
                }
                connection = (HttpURLConnection) new URL((endpoint.startsWith("https://")?"":API_SERVER)+endpoint+params).openConnection();
            }else{
                //POST, PATCH, PUT
                connection = (HttpURLConnection) new URL((endpoint.startsWith("https://")?"":API_SERVER)+endpoint).openConnection();
            }
            connection.addRequestProperty("Accept", "application/vnd.github.v3+json");
            if(authorize)GitHubRequestAuthorization.authorizeRequest(connection);
            if(method!=GitHubAPIRequestMethod.GET){
                //POST, PATCH, PUT
                byte[] postData = attributes.toJSONString().getBytes( StandardCharsets.UTF_8 );
                int postDataLength = postData.length;
                connection.setDoOutput(true);
                connection.setRequestMethod( method.name() );
                connection.setRequestProperty( "Content-Type", "application/json");
                connection.setRequestProperty( "Charset", "utf-8");
                connection.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
                connection.setUseCaches( false );
                try( DataOutputStream wr = new DataOutputStream( connection.getOutputStream())) {
                    wr.write( postData );
                }
            }
            try(InputStream in = connection.getInputStream()){
                T ret;
                try {
                    ret = (T) PARSER.parse(new InputStreamReader(in));
                }catch (ParseException e){
                    ret = null; //Invalid json content from GitHub without IOException can only mean empty output
                }
                in.close();
                return ret;
            }catch (IOException e){
                InputStream errStream = connection.getErrorStream();
                if(errStream==null)throw e; //Rethrow e to keep debug info
                InputStreamReader err = new InputStreamReader(errStream, StandardCharsets.UTF_8);
                int r;
                char[] buff = new char[1024];
                String error = "";
                while((r=err.read(buff))>0){
                    error += new String(buff, 0, r);
                }
                err.close();
                throw new IOException("Error msg: "+error, e);
            }
        } catch (Exception e) {
            throw new IOException("Error while performing request to GitHub", e);
        }
    }
}
