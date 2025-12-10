package com.ecommerce.controller.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Tag(
    name = "Error Handling",
    description = "Global handler for application errors (403, 404, 500).")
@Controller
public class CustomErrorController implements ErrorController {

  @Operation(summary = "Show error page", description = "Displays a user-friendly error page based on the HTTP status code.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Error page displayed with specific error message."),
      @ApiResponse(
          responseCode = "404",
          description = "Implicit: Handled internally when resource is not found."),
      @ApiResponse(
          responseCode = "500",
          description = "Implicit: Handled internally on server exception.")
  })
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
