
# Custom GitHub API

## Overview

This is a custom GitHub API project that allows to retrieve and list all public GitHub repositories that are not forks for the specified user.


## Run Locally

Clone the project

```bash
  git clone https://github.com/michal-bed/github-custom-api.git
```

After cloning the project you can run the app locally on your machine. You will need to run SpringBoot app on your computer.
You can use a built-in IDE functionality to achieve this. But it is also possible to run the app from the command line. For more info about running SpringBoot app from the IDE consult for instance [this article](https://www.geeksforgeeks.org/how-to-run-spring-boot-application/).




## Environment Variables

Runnig the app, you may want to set also the `GITHUB_ACCESS_TOKEN` envirnoment variable.
If you want to use this API for personal use, you can create a personal access token. For more information about creating a personal access token, see [Managing your personal access tokens](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token). 
Although it is not necessary, you can make more requests per hour when you are authenticated. Anyway, there are some restrictions regarding requests per hour imposed by an open-access GitHub REST API which this app is making use of.



## Documentation

### Headers

`Accept` string

You need to set it to `application/json`. Only this value is supported.

**Show all public repositiories that are not forks owned by the specified user**
----
  Returns repositiories info in json format

* **URL**

  /custom-github-api/repos/:username

* **Method:**

  `GET`
  
*  **URL Params**

   **Required:**
 
   `username=[string]`

* **Data Params**

  None

* **Sample Call:**

  ```javascript
    $.ajax({
      url: http://localhost:8080/custom-github-api/repos/:username 
      dataType: "json",
      type : "GET",
      success : function(r) {
        console.log(r);
      }
    });
  ```

## Usage/Examples

```curl
curl -i -H 'Accept: application/json' http://localhost:8080/custom-github-api/repos/:username
```

Similarly, it can be executed using Postman but bear in mind to substitute the `:username` with the user login whose repositories you want to check. Also, don't forget to set the apporpriate `Accept` request header.

