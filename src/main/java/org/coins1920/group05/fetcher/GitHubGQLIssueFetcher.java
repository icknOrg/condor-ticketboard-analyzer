package org.coins1920.group05.fetcher;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class GitHubGQLIssueFetcher {

    private static final String GITHUB_ROOT_URI = "https://api.github.com/graphql";

    private Logger logger = LoggerFactory.getLogger(GitHubIssueFetcher.class);

    private String oauthToken;

    public GitHubGQLIssueFetcher(String oauthToken) {
        this.oauthToken = oauthToken;
    }

    public List<Object> fetch(String owner, String board){
        // Declare connection parameters
        CloseableHttpClient client = null;
        CloseableHttpResponse response= null;
        client= HttpClients.createDefault();
        HttpPost httpPost= new HttpPost(GITHUB_ROOT_URI);
        httpPost.addHeader("Authorization", "bearer " + this.oauthToken);
        httpPost.addHeader("Accept","application/json");

        JSONObject jsonObj = new JSONObject();
        // TODO: Query ändern, sodass alles relevante gefetcht wird
        try {
            jsonObj.put("query", "{repository(owner: \""+owner+"\", name: \""+board+"\") { issues(first:5) { edges { node { title } } } } }");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            StringEntity entity= new StringEntity(jsonObj.toString());

            httpPost.setEntity(entity);
            response= client.execute(httpPost);
        }

        catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }
        catch(ClientProtocolException e){
            e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }

        try{
            BufferedReader reader= new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            //TODO: Hier irgendwo JSON Liste erstellen
            String line= null;
            StringBuilder builder= new StringBuilder();
            while((line=reader.readLine())!= null){

                builder.append(line);

            }
            System.out.println(builder.toString());
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }
}
