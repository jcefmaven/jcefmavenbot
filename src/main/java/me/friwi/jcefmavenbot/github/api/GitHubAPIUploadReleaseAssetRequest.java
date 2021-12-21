package me.friwi.jcefmavenbot.github.api;

import me.friwi.jcefmavenbot.github.GitHubAPIRequest;
import me.friwi.jcefmavenbot.github.GitHubRequestAuthorization;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static me.friwi.jcefmavenbot.github.GitHubAPIRequestMethod.PATCH;
import static me.friwi.jcefmavenbot.github.GitHubAPIRequestMethod.POST;

public class GitHubAPIUploadReleaseAssetRequest<T> extends GitHubAPIRequest<T> {
    private String filename;
    private byte[] data;

    public GitHubAPIUploadReleaseAssetRequest(GitHubRelease release, String filename, byte[] data) {
        super(POST, release.getUploadUrl().substring(0, release.getUploadUrl().indexOf("{"))+"?name=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
        this.filename = filename;
        this.data = data;
    }

    public T performRequest(boolean authorize) throws IOException {
        String charset = "UTF-8";
        String CRLF = "\r\n"; // Line separator required by multipart/form-data.

        HttpURLConnection connection = (HttpURLConnection) new URL(this.endpoint).openConnection();
        connection.setDoOutput(true);
        connection.addRequestProperty("Accept", "application/vnd.github.v3+json");
        if(authorize) GitHubRequestAuthorization.authorizeRequest(connection);
        connection.setRequestProperty("Content-Type", "text/plain");

        try (
                OutputStream output = connection.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
        ) {
            // Send text file.
            output.write(data);
            output.flush(); // Important before continuing with writer!
            writer.write(CRLF);
            writer.flush();
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
    }
}
