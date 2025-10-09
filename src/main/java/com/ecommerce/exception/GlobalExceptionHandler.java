package com.ecommerce.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InsufficientStockException.class)
    public ModelAndView handleInsufficientStockException(HttpServletRequest request,
                                                         InsufficientStockException ex) {
        log.warn("Insufficient stock for request: {}. Details: {}",
            request.getRequestURI(), ex.getMessage());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorMessage", ex.getMessage());
        modelAndView.addObject("statusCode", "400");
        modelAndView.setViewName("public/error");
        return modelAndView;
    }

    @ExceptionHandler(EmptyCartOrderException.class)
    public ModelAndView handleEmptyCartOrderException(HttpServletRequest request,
                                                      EmptyCartOrderException ex) {
        log.warn("Attempted to order with an empty cart: {}. Details: {}",
            request.getRequestURI(), ex.getMessage());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorMessage", ex.getMessage());
        modelAndView.addObject("statusCode", "400");
        modelAndView.setViewName("public/error");
        return modelAndView;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleResourceNotFoundException(HttpServletRequest request,
                                                        ResourceNotFoundException ex) {
        log.warn("Resource not found for request: {}. Details: {}",
            request.getRequestURI(), ex.getMessage());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorMessage", ex.getMessage());
        modelAndView.addObject("statusCode", "404");
        modelAndView.setViewName("public/error");
        return modelAndView;
    }

    @ExceptionHandler(ConfigurationException.class)
    public ModelAndView handleConfigurationException(HttpServletRequest request,
                                                     ConfigurationException ex) {
        log.error("Configuration error for request: {}", request.getRequestURI(), ex);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorMessage", ex.getMessage());
        modelAndView.addObject("statusCode", "500");
        modelAndView.setViewName("public/error");
        return modelAndView;
    }

    @ExceptionHandler(ReviewReadditionException.class)
    public ModelAndView handleReviewReadditionException(HttpServletRequest request,
                                                        ReviewReadditionException ex) {
        log.warn("Review re-addition attempt for request: {}. Details: {}",
            request.getRequestURI(), ex.getMessage());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorMessage", "An unexpected error occurred: " + ex.getMessage());
        modelAndView.addObject("statusCode", "500");
        modelAndView.setViewName("public/error");
        return modelAndView;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDeniedException(HttpServletRequest request,
                                                    AccessDeniedException ex) {
        log.warn("Access denied for request: {}. Details: {}",
            request.getRequestURI(), ex.getMessage());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorMessage", "An unexpected error occurred: " + ex.getMessage());
        modelAndView.addObject("statusCode", "403");
        modelAndView.setViewName("public/error");
        return modelAndView;
    }

    @ExceptionHandler(AddressLimitExceededException.class)
    public ModelAndView handleReviewReadditionException(HttpServletRequest request,
                                                        AddressLimitExceededException ex) {
        log.warn("Address limit exceeded for request: {}. Details: {}",
            request.getRequestURI(), ex.getMessage());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorMessage", "An unexpected error occurred: " + ex.getMessage());
        modelAndView.addObject("statusCode", "500");
        modelAndView.setViewName("public/error");
        return modelAndView;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView handleNoResourceFoundException(HttpServletRequest request,
                                                       NoResourceFoundException ex) {
        log.warn("No resource found for request: {}. Details: {}",
            request.getRequestURI(), ex.getMessage());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorMessage", "An unexpected error occurred: " + ex.getMessage());
        modelAndView.addObject("statusCode", "404");
        modelAndView.setViewName("public/error");
        return modelAndView;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleAllExceptions(HttpServletRequest request, Exception ex) {
        log.error("Unhandled exception for request: {}", request.getRequestURI(), ex);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorMessage", "An unexpected error occurred: " + ex.getMessage());
        modelAndView.addObject("statusCode", "500");
        modelAndView.setViewName("public/error");
        return modelAndView;
    }

    @ExceptionHandler(UserNotAuthenticatedException.class)
    public String handleUserNotAuthenticated(HttpServletRequest request) {
        String originalUrl = request.getRequestURI();
        if (request.getQueryString() != null) {
            originalUrl += "?" + request.getQueryString();
        }
        log.info("User not authenticated. Redirecting to login for requested URL: {}", originalUrl);
        return "redirect:/login?redirectUrl=" + originalUrl;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleConstraintViolationException(
            ConstraintViolationException ex, RedirectAttributes redirectAttributes) {
        String errorMessage = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        log.warn("Constraint violation on request parameters: {}", errorMessage);

        redirectAttributes.addFlashAttribute(
                "errorMessage", "Invalid search parameters provided. Please try again.");

        return "redirect:/products/list";
    }
}
