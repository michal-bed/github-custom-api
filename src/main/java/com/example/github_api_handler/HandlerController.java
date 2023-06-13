package com.example.github_api_handler;

import com.example.github_api_handler.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

@RestController()
public class HandlerController {

    static private final String GITHUB_ACCESS_TOKEN =
            System.getenv().getOrDefault("GITHUB_ACCESS_TOKEN", "");

    @GetMapping(value = "/custom-github-api/repos/{username}", produces = { "application/json" })
    ResponseEntity<CustomResponseInterface> getALlGithubReposInfoForUser(@PathVariable String username,
                                                                        @RequestHeader("Accept") String acceptHeader)
            throws URISyntaxException, IOException, InterruptedException {

        ArrayList<CustomResponseGithubRepository> responseRepos = new ArrayList<>();
        for (int page = 1; ;page++) {

            var repos = getGithubRepositoriesForUser(username, page);
            if (page == 1 && repos == null) {
                return ResponseEntity.status(404).body(new ErrorApiResponse(404, "Not Found"));
            }
            else if (repos == null || repos.isEmpty())
            {
                var customResponse = new CustomApiResponse(responseRepos);
                //        System.out.println("customResponse = " + customResponse);
                return ResponseEntity.status(HttpStatus.OK).body(customResponse);
            }

            for (var repo : repos) {
                ArrayList<GithubBranch> branches = new ArrayList<>();
                var branches_url = repo.getBranches_url();
                HttpRequest request = buildApiRequest(branches_url);

                HttpResponse<String> response = getStringHttpResponse(request);

                JsonNode body = getJsonNodeBody(response);

                var elements = body.elements();
                while (elements.hasNext()) {
                    JsonNode branch = elements.next();
                    //                System.out.println("branch = " + branch);
                    var githubBranch =
                            new GithubBranch(branch.get("name").textValue(),
                                    new GithubCommit((branch.get("commit").get("sha").textValue())));
                    branches.add(githubBranch);
                }

                responseRepos.add(
                        new CustomResponseGithubRepository(repo.getName(),
                                repo.getOwner(), branches));
            }
        }

    }

    private HttpRequest buildApiRequest(String branches_url) throws URISyntaxException {
        return HttpRequest.newBuilder()
                .uri(new URI(branches_url))
                .headers("Accept", "application/vnd.github+json",
                        "Authentication", String.format("Bearer %s", GITHUB_ACCESS_TOKEN))
                .GET()
                .build();
    }

    private ArrayList<GithubRepository> getGithubRepositoriesForUser(String username, int page) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(String.format("https://api.github.com/users/%s/repos?per_page=100&page=%s", username, page)))
                .headers("Accept", "application/vnd.github+json",
                        "Authentication", String.format("Bearer %s", GITHUB_ACCESS_TOKEN))
                .GET()
                .build();

        HttpResponse<String> response = getStringHttpResponse(request);

        JsonNode body = getJsonNodeBody(response);
        if (body.size() == 0)
        {
            return null;
        }

        var message = body.get("message");
        if (message != null && message.textValue().equals("Not Found"))
        {
            return null;
        }
        ArrayList<GithubRepository> repos = new ArrayList<>();
        var elements = body.elements();
        while (elements.hasNext()) {
            JsonNode repo = elements.next();
//            System.out.println("repo = " + repo);
            if(repo.get("fork") != null && !repo.get("fork").booleanValue()) {
                var branches_url = repo.get("branches_url").textValue();
                branches_url = branches_url.substring(0, branches_url.length() - 9);
                var githubRepo =
                        new GithubRepository(repo.get("name").textValue(),
                                repo.get("owner").get("login").textValue(),
                                branches_url);
                repos.add(githubRepo);
//                System.out.println("githubRepo = " + githubRepo);
            }
        }

        return repos;

    }

    private JsonNode getJsonNodeBody(HttpResponse<String> response) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        return mapper.readTree(response.body());
    }

    private HttpResponse<String> getStringHttpResponse(HttpRequest request) throws IOException, InterruptedException {
        return HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
    }
}
