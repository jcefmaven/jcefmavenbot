package me.friwi.jcefmavenbot.github;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class GitHubRequestAuthorization {
    private static String AUTHORIZATION;

    public static void init(String user, String token){
        AUTHORIZATION = "Basic " + new String(
                Base64.getEncoder().encode(
                        (user + ":" + token).getBytes()
                ), StandardCharsets.UTF_8
        );
    }

    public static void authorizeRequest(HttpURLConnection connection){
        connection.setRequestProperty("Authorization", AUTHORIZATION);
    }
}
