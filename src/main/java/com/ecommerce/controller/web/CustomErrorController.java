package com.ecommerce.controller.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomErrorController implements ErrorController {
    @GetMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String statusCode = "N/A";
        String errorMessage = "An unexpected error occurred";

        if (status != null) {
            int statusCodeInt = Integer.parseInt(status.toString());
            statusCode = Integer.toString(statusCodeInt);

            if (statusCodeInt == HttpStatus.NOT_FOUND.value()) {
                errorMessage = "The page you are looking for could not be found.";
            } else if (statusCodeInt == HttpStatus.FORBIDDEN.value()) {
                errorMessage = "You are not authorized to access this page.";
            } else if (statusCodeInt == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                errorMessage = "An internal server error occurred. Please try again later.";
            }
        }

        model.addAttribute("statusCode", statusCode);
        model.addAttribute("errorMessage", errorMessage);
        return "public/error";
    }
}
