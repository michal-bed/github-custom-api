package com.example.github_api_handler.model;

import java.io.Serializable;

public class ErrorApiResponse implements CustomResponseInterface  {
    private int status;
    private String message;

    public ErrorApiResponse(int status, String message) {
        super();
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
