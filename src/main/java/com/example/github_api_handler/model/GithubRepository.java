package com.example.github_api_handler.model;

import java.io.Serializable;

public class GithubRepository implements Serializable {
    private String name;
    private String owner;
    private String branchesUrl;

    public GithubRepository(String name, String owner, String branches_url) {
        this.name = name;
        this.owner = owner;
        this.branchesUrl = branches_url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getBranchesUrl() {
        return branchesUrl;
    }

    public void setBranchesUrl(String branchesUrl) {
        this.branchesUrl = branchesUrl;
    }
}
