package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GitHubActivityCLI {
    public static void main(String[] args) {
        if (args.length != 1){
            System.out.println("Usage: java GitHubAcivityCLI <username>");
            return;
        }
        GitHubActivityCLI cli = new GitHubActivityCLI();
        cli.fetchGitHubAcitivity(args[0]);
    }

    private void fetchGitHubAcitivity(String username){
        String GITHUB_API_URL = "https://api.github.com/users/"+ username + "/events";

        HttpClient client = HttpClient.newHttpClient();
        try{
            HttpRequest request = HttpRequest.newBuilder().uri(new URI(GITHUB_API_URL)).header("Accept", "application/vnd.github+json").GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404){
                System.out.println("User not found. Please check the username.");
                return;
            }
            if (response.statusCode() == 200){
                JsonParser parser = new JsonParser();
                JsonArray jsonArray = parser.parseString(response.body()).getAsJsonArray();
                displayActivity(jsonArray);
            } else {
                System.out.println("Error:" + response.statusCode());
            }
        } catch (URISyntaxException uriSyntaxException){
            uriSyntaxException.printStackTrace();
        } catch (IOException ioException){
            ioException.printStackTrace();
        } catch (InterruptedException interruptedException){
            Thread.currentThread().interrupt();
            interruptedException.printStackTrace();
        }
    }

    private void displayActivity(JsonArray events){
        for (JsonElement element : events){
            JsonObject event = element.getAsJsonObject();
            String type = event.get("type").getAsString();
            String action;
            switch (type){
                case "PushEvent":
                    int commitCount = event.get("payload").getAsJsonObject().get("commits").getAsJsonArray().size();
                    action = "Pushed " + commitCount + " commit(s) to " + event.get("repo").getAsJsonObject().get("name");
                    break;
                case "IssuesEvent":
                    action = event.get("payload").getAsJsonObject().get("action").getAsString().toUpperCase().charAt(0)
                            + event.get("payload").getAsJsonObject().get("action").getAsString()
                            + " an issue in ${event.repo.name}";
                    break;
                case "WatchEvent":
                    action = "Starred " + event.get("repo").getAsJsonObject().get("name").getAsString();
                    break;
                case "ForkEvent":
                    action = "Forked " + event.get("repo").getAsJsonObject().get("name").getAsString();
                    break;
                case "CreateEvent":
                    action = "Created " + event.get("payload").getAsJsonObject().get("ref_type").getAsString()
                            + " in " + event.get("repo").getAsJsonObject().get("name").getAsString();

                    break;
                default:
                    action = event.get("type").getAsString().replace("Event", "")
                            + " in " + event.get("repo").getAsJsonObject().get("name").getAsString();
                    break;
            }
            System.out.println(action);
        }
    }
}

