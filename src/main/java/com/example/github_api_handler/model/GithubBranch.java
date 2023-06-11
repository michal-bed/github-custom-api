package com.example.github_api_handler.model;

import java.io.Serializable;

public class GithubBranch implements Serializable {
    private String name;
    private GithubCommit commit;

    public GithubBranch(String name, GithubCommit commit) {
        this.name = name;
        this.commit = commit;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GithubCommit getCommit() {
        return commit;
    }

    public void setCommit(GithubCommit commit) {
        this.commit = commit;
    }
}
