package com.launchiq.controller;

import com.launchiq.service.DBHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

@Controller
public class AuthController {

    @GetMapping("/admin-login")
    public String adminLoginPage() {
        return "admin-login";
    }

    @PostMapping("/admin/login")
    public String adminLogin(@RequestParam String email,
                             @RequestParam String password,
                             HttpServletRequest request) {
        try {
            Map<String,Object> user = DBHelper.validateUser(email, password);
            if (user != null) {
                HttpSession session = request.getSession(true);
                session.setAttribute("loggedInUser", user.get("name"));
                session.setAttribute("loggedInEmail", user.get("email"));
                return "redirect:/dashboard";
            }
        } catch (Exception ignored) {}
        return "redirect:/admin-login?error=1";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return "redirect:/admin-login";
    }
}


