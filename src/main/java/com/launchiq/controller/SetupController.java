package com.launchiq.controller;

import com.launchiq.service.DBHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SetupController {

	@GetMapping({"/setup","/Setup"})
	public String setupPage() {
		return "setup";
	}

	@PostMapping({"/setup","/Setup"})
	public String handleSetup(@RequestParam String name,
							  @RequestParam String email,
							  @RequestParam String password,
							  @RequestParam String confirm) {
		try {
			String n = name == null ? "" : name.trim();
			String e = email == null ? "" : email.trim();
			if (n.isEmpty() || e.isEmpty()) {
				return "redirect:/setup?error=Missing+name+or+email";
			}
			if (!password.equals(confirm)) {
				return "redirect:/setup?error=PasswordMismatch";
			}
			DBHelper.createUser(n, e, password);
			return "redirect:/admin-login?setup=success";
		} catch (Exception ex) {
			return "redirect:/setup?error=SetupFailed";
		}
	}
}


