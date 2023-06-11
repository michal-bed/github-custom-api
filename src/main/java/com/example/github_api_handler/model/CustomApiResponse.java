package com.example.github_api_handler.model;

import java.io.Serializable;
import java.util.List;

public class CustomApiResponse implements CustomResponseInterface {

  private List<CustomResponseGithubRepository> repositories;

  public CustomApiResponse(List<CustomResponseGithubRepository> repositories) {
    this.repositories = repositories;
  }
//
//  CustomApiResponse() {
//
//  }

  public void setRepositories(List<CustomResponseGithubRepository> repositories) {
    this.repositories = repositories;
  }


  public List<CustomResponseGithubRepository> getRepositories() {
    return repositories;
  }
}
