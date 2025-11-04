package com.launchiq.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class ViewController {

    @GetMapping({"/", "/dashboard"})
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/launcher")
    public String launcher() {
        return "launcher";
    }

    // setup page handled by SetupController (supports both GET/POST and case variants)

    @GetMapping("/login")
    public String loginRedirect() {
        return "redirect:/admin-login";
    }
}


