package com.ecommerce.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InsufficientStockException.class)
    public ModelAndView handleInsufficientStockException(HttpServletRequest request,
                                                         InsufficientStockException ex) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorMessage", ex.getMessage());
        modelAndView.addObject("statusCode", "400");
        modelAndView.setViewName("public/error");
        return modelAndView;
    }

    @ExceptionHandler(EmptyCartOrderException.class)
    public ModelAndView handleEmptyCartOrderException(HttpServletRequest request,
                                                      EmptyCartOrderException ex) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorMessage", ex.getMessage());
        modelAndView.addObject("statusCode", "400");
        modelAndView.setViewName("public/error");
        return modelAndView;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleResourceNotFoundException(HttpServletRequest request,
                                                        ResourceNotFoundException ex) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorMessage", ex.getMessage());
        modelAndView.addObject("statusCode", "404");
        modelAndView.setViewName("public/error");
        return modelAndView;
    }

    @ExceptionHandler(ConfigurationException.class)
    public ModelAndView handleConfigurationException(HttpServletRequest request,
                                                     ConfigurationException ex) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorMessage", ex.getMessage());
        modelAndView.addObject("statusCode", "500");
        modelAndView.setViewName("public/error");
        return modelAndView;
    }

    @ExceptionHandler(ReviewReadditionException.class)
    public ModelAndView handleReviewReadditionException(HttpServletRequest request,
                                                        ReviewReadditionException ex) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorMessage", "An unexpected error occurred: " + ex.getMessage());
        modelAndView.addObject("statusCode", "500");
        modelAndView.setViewName("public/error");
        return modelAndView;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDeniedException(HttpServletRequest request,
                                                    AccessDeniedException ex) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorMessage", "An unexpected error occurred: " + ex.getMessage());
        modelAndView.addObject("statusCode", "403");
        modelAndView.setViewName("public/error");
        return modelAndView;
    }

    @ExceptionHandler(AddressLimitExceededException.class)
    public ModelAndView handleReviewReadditionException(HttpServletRequest request,
                                                        AddressLimitExceededException ex) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorMessage", "An unexpected error occurred: " + ex.getMessage());
        modelAndView.addObject("statusCode", "500");
        modelAndView.setViewName("public/error");
        return modelAndView;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView handleNoResourceFoundException(HttpServletRequest request,
                                                       NoResourceFoundException ex) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorMessage", "An unexpected error occurred: " + ex.getMessage());
        modelAndView.addObject("statusCode", "404");
        modelAndView.setViewName("public/error");
        return modelAndView;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleAllExceptions(HttpServletRequest request, Exception ex) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorMessage", "An unexpected error occurred: " + ex.getMessage());
        modelAndView.addObject("statusCode", "500");
        modelAndView.setViewName("public/error");
        return modelAndView;
    }
}
