package com.launchiq.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ModelAndView handleAnyException(Exception ex, HttpServletRequest request) {
        String uri = request.getRequestURI();
        String target = "/admin-login?error=Unexpected";
        if (uri != null && (uri.equalsIgnoreCase("/setup") || uri.equalsIgnoreCase("/Setup"))) {
            target = "/setup?error=SetupFailed";
        }
        ModelAndView mv = new ModelAndView("redirect:" + target);
        return mv;
    }
}


