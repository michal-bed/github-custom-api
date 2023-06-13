package com.example.github_api_handler;

import com.example.github_api_handler.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    ResponseEntity<CustomResponseInterface> getALlGithubReposInfoForUser(@PathVariable String username,
                                                                        @RequestHeader("Accept") String acceptHeader)
            throws URISyntaxException, IOException, InterruptedException {

        ArrayList<CustomResponseGithubRepository> responseRepos = new ArrayList<>();
        for (int page = 1; ;page++) {

            var repos = getGithubRepositoriesForUser(username, page);
            ResponseEntity<CustomResponseInterface> finalResponse =
                    getResponse(page, repos);
            if (finalResponse != null) return finalResponse;

            assert repos != null;
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

    }

    private void getAllBranchesForGivenUrl(List<GithubBranch> branches, JsonNode body) {
        var elements = body.elements();
        while (elements.hasNext()) {
            JsonNode branch = elements.next();
            //                System.out.println("branch = " + branch);
            assert branch.get("name") != null;
            assert branch.get("commit") != null;
            assert branch.get("commit").get("sha") != null;
            var githubBranch =
                    new GithubBranch(branch.get("name").textValue(),
                            new GithubCommit((branch.get("commit").get("sha").textValue())));
            branches.add(githubBranch);
        }
    }

    private JsonNode getJsonNode(String branches_url) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = buildApiRequest(branches_url);

        HttpResponse<String> response = getStringHttpResponse(request);

        return getJsonNodeBody(response);
    }

    private ResponseEntity<CustomResponseInterface> getResponse
            (int page, List<GithubRepository> repos) {
        if ((page == 1 && repos == null) || (page != 1 && repos.isEmpty())) {
            return ResponseEntity.status(404).body(
                    new ErrorApiResponse(404, errorMessage.orElse("Unrecognized cause")));
        }
        return null;
    }

    private HttpRequest buildApiRequest(String branches_url) throws URISyntaxException {
        return HttpRequest.newBuilder()
                .uri(new URI(branches_url))
                .headers("Accept", "application/vnd.github+json",
                        "Authentication", String.format("Bearer %s", GITHUB_ACCESS_TOKEN))
                .GET()
                .build();
    }

    private List<GithubRepository> getGithubRepositoriesForUser(String username, int page) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(String.format("https://api.github.com/users/%s/repos?per_page=100&page=%s", username, page)))
                .headers("Accept", "application/vnd.github+json",
                        "Authentication", String.format("Bearer %s", GITHUB_ACCESS_TOKEN))
                .GET()
                .build();

        HttpResponse<String> response = getStringHttpResponse(request);

        JsonNode body = getJsonNodeBody(response);
        if (isEmptyOrErrorOccurred(body)) return null;
        ArrayList<GithubRepository> repos = new ArrayList<>();
        var elements = body.elements();
        getPublicReposThatAreNotForks(repos, elements);

        return repos;

    }

    private void getPublicReposThatAreNotForks(ArrayList<GithubRepository> repos, Iterator<JsonNode> elements) {
        while (elements.hasNext()) {
            JsonNode repo = elements.next();
//            System.out.println("repo = " + repo);
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
