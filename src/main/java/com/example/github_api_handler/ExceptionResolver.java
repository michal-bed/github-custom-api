package com.example.github_api_handler;

import com.example.github_api_handler.model.ErrorApiResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ExceptionResolver extends ResponseEntityExceptionHandler {

    @Override
    @ResponseBody()
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        var errorDetails = new ErrorApiResponse(406, "Not Supported Accept Header");
        var customHeaders = new HttpHeaders();
        customHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(
                errorDetails, customHeaders, 406);
    }
}
