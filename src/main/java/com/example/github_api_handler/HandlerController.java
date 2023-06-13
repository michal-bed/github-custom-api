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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@RestController()
public class HandlerController {

    static private final String GITHUB_ACCESS_TOKEN =
            System.getenv().getOrDefault("GITHUB_ACCESS_TOKEN", "");
    static private Optional<String> errorMessage = Optional.empty();

    @GetMapping(value = "/custom-github-api/repos/{username}", produces = { "application/json" })
    ResponseEntity<CustomResponseInterface> getAllGithubReposInfoForUser(@PathVariable String username)
            throws URISyntaxException, IOException, InterruptedException {

        return getCustomResponseEntity(username);

    }

    private ResponseEntity<CustomResponseInterface>
        getCustomResponseEntity(String username) throws URISyntaxException, IOException, InterruptedException {
        ArrayList<CustomResponseGithubRepository> responseRepos = new ArrayList<>();
        for (int page = 1; ;page++) {

            var repos = getGithubRepositoriesForUser(username, page);
            ResponseEntity<CustomResponseInterface> finalResponse =
                    getResponse(responseRepos, repos);
            if (finalResponse != null) return finalResponse;

            assert repos != null;
            addReposInfoToResponseRepos(responseRepos, repos);
        }
    }

    private void addReposInfoToResponseRepos(ArrayList<CustomResponseGithubRepository> responseRepos, List<GithubRepository> repos) throws URISyntaxException, IOException, InterruptedException {
        for (var repo : repos) {
            ArrayList<GithubBranch> branches = new ArrayList<>();
            var branchesUrl = repo.getBranchesUrl();
            JsonNode body = getJsonNode(branchesUrl);

            getAllBranchesForGivenUrl(branches, body);

            responseRepos.add(
                    new CustomResponseGithubRepository(repo.getName(),
                            repo.getOwner(), branches));
        }
    }

    private void getAllBranchesForGivenUrl(List<GithubBranch> branches, JsonNode body) {
        var elements = body.elements();
        while (elements.hasNext()) {
            JsonNode branch = elements.next();
            //                System.out.println("branch = " + branch);
            executeRepoOrBranchAsserts(branch, "commit", "sha");
            var githubBranch =
                    new GithubBranch(branch.get("name").textValue(),
                            new GithubCommit((branch.get("commit").get("sha").textValue())));
            branches.add(githubBranch);
        }
    }

    private JsonNode getJsonNode(String branchesUrl) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = buildApiRequest(branchesUrl);

        HttpResponse<String> response = getStringHttpResponse(request);

        return getJsonNodeBody(response);
    }

    private ResponseEntity<CustomResponseInterface> getResponse
            (List<CustomResponseGithubRepository> responseRepos,
             List<GithubRepository> repos) {
        if (repos == null && errorMessage.isPresent()) {
            var errorResponse =
                    new ErrorApiResponse(404, errorMessage.orElse("Unrecognized cause"));
            errorMessage = Optional.empty();
            return ResponseEntity.status(404).body(errorResponse);
        }
        else if (repos == null)
        {
            var customResponse = new CustomApiResponse(responseRepos);
            //        System.out.println("customResponse = " + customResponse);
            return ResponseEntity.status(HttpStatus.OK).body(customResponse);
        }
        return null;
    }

    private HttpRequest buildApiRequest(String branches_url) throws URISyntaxException {
        return HttpRequest.newBuilder()
                .uri(new URI(branches_url))
                .headers("Accept", "application/vnd.github+json",
                        "Authorization", String.format("Bearer %s", GITHUB_ACCESS_TOKEN))
                .GET()
                .build();
    }

    private List<GithubRepository> getGithubRepositoriesForUser(String username, int page) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = getGithubUserRepositoriesHttpRequest(username, page);

        HttpResponse<String> response = getStringHttpResponse(request);

        JsonNode body = getJsonNodeBody(response);
        if (isEmptyOrErrorOccurred(body)) return null;
        ArrayList<GithubRepository> repos = new ArrayList<>();
        var elements = body.elements();
        getPublicReposThatAreNotForks(repos, elements);

        return repos;

    }

    private HttpRequest getGithubUserRepositoriesHttpRequest(String username, int page) throws URISyntaxException {
        return HttpRequest.newBuilder()
                .uri(new URI(String.format("https://api.github.com/users/%s/repos?per_page=100&page=%s", username, page)))
                .headers("Accept", "application/vnd.github+json",
                        "Authorization", String.format("Bearer %s", GITHUB_ACCESS_TOKEN))
                .GET()
                .build();
    }

    private void getPublicReposThatAreNotForks(ArrayList<GithubRepository> repos, Iterator<JsonNode> elements) {
        while (elements.hasNext()) {
            JsonNode repo = elements.next();
//            System.out.println("repo = " + repo);
            executeRepoOrBranchAsserts(repo, "name", "login");
            if(repo.get("fork") != null && !repo.get("fork").booleanValue()) {
                var branchesUrl = repo.get("branches_url").textValue();
                branchesUrl = branchesUrl.substring(0, branchesUrl.length() - 9);
                var githubRepo =
                        new GithubRepository(repo.get("name").textValue(),
                                repo.get("owner").get("login").textValue(),
                                branchesUrl);
                repos.add(githubRepo);
//                System.out.println("githubRepo = " + githubRepo);
            }
        }
    }

    private void executeRepoOrBranchAsserts(JsonNode node, String arg1, String arg2) {
        assert node.get("name") != null;
        assert node.get(arg1) != null;
        assert node.get(arg1).get(arg2) != null;
    }

    private boolean isEmptyOrErrorOccurred(JsonNode body) {
        if (body.size() == 0) {
            return true;
        }

        var message = body.get("message");
        if (message != null) {
            errorMessage = Optional.of(message.textValue());
            return true;
        }
        return false;
    }


    private JsonNode getJsonNodeBody(HttpResponse<String> response) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        return mapper.readTree(response.body());
    }

    private HttpResponse<String> getStringHttpResponse(HttpRequest request)
            throws IOException, InterruptedException {
        return HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
    }
}
