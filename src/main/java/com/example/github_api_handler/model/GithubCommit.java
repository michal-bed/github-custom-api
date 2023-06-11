package com.example.github_api_handler.model;

import java.io.Serializable;

public class GithubCommit implements Serializable {

    private String lastCommitSha;

    public GithubCommit(String lastCommitSha) {
        this.lastCommitSha = lastCommitSha;
    }

    public void setLastCommitSha(String lastCommitSha) {
        this.lastCommitSha = lastCommitSha;
    }

    public String getLastCommitSha() {
        return lastCommitSha;
    }
}
