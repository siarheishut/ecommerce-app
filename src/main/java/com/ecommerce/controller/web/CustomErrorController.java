package com.ecommerce.controller.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class CustomErrorController implements ErrorController {
  @GetMapping("/error")
  public String handleError(HttpServletRequest request, Model model) {
    Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    Object requestUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
    Throwable throwable = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

    String statusCode = "N/A";
    String errorMessage = "An unexpected error occurred";

    if (status != null) {
      try {
        int statusCodeInt = Integer.parseInt(status.toString());
        statusCode = Integer.toString(statusCodeInt);

        switch (HttpStatus.valueOf(statusCodeInt)) {
          case NOT_FOUND -> {
            errorMessage = "The page you are looking for could not be found.";
            log.warn("404 Not Found for URI: {}", requestUri);
          }
          case FORBIDDEN -> {
            errorMessage = "You are not authorized to access this page.";
            log.warn("403 Forbidden for URI: {}", requestUri);
          }
          default -> {
            if (statusCodeInt >= 500) {
              errorMessage = "An internal server error occurred. Please try again later.";
              log.error("5xx Server Error for URI: {}. Status: {}", requestUri, statusCode, throwable);
            }
          }
        }
      } catch (NumberFormatException e) {
        log.error("Could not parse error status code: {}", status, e);
      }
    }

    model.addAttribute("statusCode", statusCode);
    model.addAttribute("errorMessage", errorMessage);
    return "public/error";
  }
}
