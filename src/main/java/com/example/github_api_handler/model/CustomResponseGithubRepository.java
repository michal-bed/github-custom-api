package com.example.github_api_handler.model;

import java.io.Serializable;
import java.util.List;

public class CustomResponseGithubRepository implements Serializable {

    private String repositoryName;
    private String ownerLogin;
    private List<GithubBranch> branches;

    public CustomResponseGithubRepository(String repositoryName, String ownerLogin, List<GithubBranch> branches) {
        this.repositoryName = repositoryName;
        this.ownerLogin = ownerLogin;
        this.branches = branches;
    }

    public CustomResponseGithubRepository() {

    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getOwnerLogin() {
        return ownerLogin;
    }

    public void setOwnerLogin(String ownerLogin) {
        this.ownerLogin = ownerLogin;
    }

    public List<GithubBranch> getBranches() {
        return branches;
    }

    public void setBranches(List<GithubBranch> branches) {
        this.branches = branches;
    }
}
